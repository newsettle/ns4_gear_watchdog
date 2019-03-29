/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.creditease.ns4.gear.watchdog.monitor.collect.log.source.taildir;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.monitor.collect.log.constant.TaildirSourceConfigurationConstants;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.gson.stream.JsonReader;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.annotations.InterfaceAudience;
import org.apache.flume.annotations.InterfaceStability;
import org.apache.flume.client.avro.ReliableEventReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author outman
 * @description 扩展flume taildirSource
 * @date 2019/3/7
 */
@InterfaceAudience.Private
@InterfaceStability.Evolving
public class ReliableTaildirEventReader implements ReliableEventReader {

    private static final NsLog logger = NsLogger.getWatchdogCollectLogger();

    private final List<TaildirMatcher> taildirCache;
    private final Table<String, String, String> headerTable;

    private TailFile currentFile = null;
    private Map<Long, TailFile> tailFiles = Maps.newHashMap();
    private long updateTime;
    private boolean addByteOffset;
    private boolean cachePatternMatching;
    private boolean committed = true;
    private final boolean annotateFileName;
    private final String fileNameHeader;

    /**
     * Create a ReliableTaildirEventReader to watch the given directory.
     */
    private ReliableTaildirEventReader(Map<String, String> filePaths,
                                       Map<String, String[]> exclusiveFiles,
                                       long fileExpiredTime,
                                       Table<String, String, String> headerTable, String positionFilePath,
                                       boolean skipToEnd, boolean addByteOffset, boolean cachePatternMatching,
                                       boolean annotateFileName, String fileNameHeader) throws IOException {
        // Sanity checks
        Preconditions.checkNotNull(filePaths);
        Preconditions.checkNotNull(positionFilePath);

        if (logger.isDebugEnabled()) {
            logger.debug("Initializing {} with directory={}, metaDir={}",
                    new Object[]{ReliableTaildirEventReader.class.getSimpleName(), filePaths});
        }

        List<TaildirMatcher> taildirCache = Lists.newArrayList();
        for (Map.Entry<String, String> e : filePaths.entrySet()) {
            taildirCache.add(new TaildirMatcher(e.getKey(), e.getValue(), exclusiveFiles.get(e.getKey()), fileExpiredTime, cachePatternMatching));
        }
        logger.info("taildirCache: " + taildirCache.toString());
        logger.info("headerTable: " + headerTable.toString());

        this.taildirCache = taildirCache;
        this.headerTable = headerTable;
        this.addByteOffset = addByteOffset;
        this.cachePatternMatching = cachePatternMatching;
        this.annotateFileName = annotateFileName;
        this.fileNameHeader = fileNameHeader;
        updateTailFiles(skipToEnd);

        logger.info("Updating position from position file: " + positionFilePath);
        loadPositionFile(positionFilePath);
    }

    /**
     * Load a position file which has the last read position of each file.
     * If the position file exists, update tailFiles mapping.
     */
    public void loadPositionFile(String filePath) {
        Long inode, pos, lineNum;
        String path;
        FileReader fr = null;
        JsonReader jr = null;
        try {
            fr = new FileReader(filePath);
            jr = new JsonReader(fr);
            jr.beginArray();
            while (jr.hasNext()) {
                inode = null;
                pos = null;
                path = null;
                lineNum = null;
                jr.beginObject();
                while (jr.hasNext()) {
                    switch (jr.nextName()) {
                        case "inode":
                            inode = jr.nextLong();
                            break;
                        case "pos":
                            pos = jr.nextLong();
                            break;
                        case "lineNum":
                            lineNum = jr.nextLong();
                            break;
                        case "file":
                            path = jr.nextString();
                            break;
                    }
                }
                jr.endObject();

                for (Object v : Arrays.asList(inode, pos, path)) {
                    Preconditions.checkNotNull(v, "Detected missing value in position file. "
                            + "inode: " + inode + ", pos: " + pos + ", path: " + path);
                }
                TailFile tf = tailFiles.get(inode);
                if (tf != null && tf.updatePos(path, inode, pos, lineNum)) {
                    tailFiles.put(inode, tf);
                } else {
                    logger.info("Missing file: " + path + ", inode: " + inode + ", pos: " + pos);
                }
            }
            jr.endArray();
        } catch (FileNotFoundException e) {
            logger.info("File not found: " + filePath + ", not updating position");
        } catch (IOException e) {
            logger.error("Failed loading positionFile: " + filePath, e);
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
                if (jr != null) {
                    jr.close();
                }
            } catch (IOException e) {
                logger.error("Error: " + e.getMessage(), e);
            }
        }
    }

    public Map<Long, TailFile> getTailFiles() {
        return tailFiles;
    }

    public void setCurrentFile(TailFile currentFile) {
        this.currentFile = currentFile;
    }

    @Override
    public Event readEvent() throws IOException {
        List<Event> events = readEvents(1);
        if (events.isEmpty()) {
            return null;
        }
        return events.get(0);
    }

    @Override
    public List<Event> readEvents(int numEvents) throws IOException {
        return readEvents(numEvents, false);
    }

    @VisibleForTesting
    public List<Event> readEvents(TailFile tf, int numEvents) throws IOException {
        setCurrentFile(tf);
        return readEvents(numEvents, true);
    }

    public List<Event> readEvents(int numEvents, boolean backoffWithoutNL)
            throws IOException {
        if (!committed) {
            if (currentFile == null) {
                throw new IllegalStateException("current file does not exist. " + currentFile.getPath());
            }
            logger.info("Last read was never committed - resetting position");
            long lastPos = currentFile.getPos();
            currentFile.updateFilePos(lastPos);
        }
        List<Event> events = currentFile.readEvents(numEvents, backoffWithoutNL, addByteOffset);
        if (events.isEmpty()) {
            return events;
        }

        Map<String, String> headers = currentFile.getHeaders();
        if (annotateFileName || (headers != null && !headers.isEmpty())) {
            for (Event event : events) {
                if (headers != null && !headers.isEmpty()) {
                    event.getHeaders().putAll(headers);
                }
                if (annotateFileName) {
                    event.getHeaders().put(fileNameHeader, currentFile.getPath());
                }
            }
        }
        committed = false;
        return events;
    }

    @Override
    public void close() throws IOException {
        for (TailFile tf : tailFiles.values()) {
            if (tf.getRaf() != null) {
                tf.getRaf().close();
            }
        }
    }

    /**
     * Commit the last lines which were read.
     */
    @Override
    public void commit() throws IOException {
        if (!committed && currentFile != null) {
            long pos = currentFile.getLineReadPos();
            currentFile.setPos(pos);
            currentFile.setLastUpdated(updateTime);
            committed = true;
        }
    }

    /**
     * Update tailFiles mapping if a new file is created or appends are detected
     * to the existing file.
     */
    public List<Long> updateTailFiles(boolean skipToEnd) throws IOException {
        updateTime = System.currentTimeMillis();
        List<Long> updatedInodes = Lists.newArrayList();

        for (TaildirMatcher taildir : taildirCache) {
            Map<String, String> headers = headerTable.row(taildir.getFileGroup());

            for (File f : taildir.getMatchingFiles()) {
                long inode = getInode(f);
                TailFile tf = tailFiles.get(inode);
                //modify by outman 只用判断文件不为空即可，不用判断文件的名字，因为日志切分文件会重命名文件
                if (tf == null/* || !tf.getPath().equals(f.getAbsolutePath())*/) {
                    long startPos = skipToEnd ? f.length() : 0;
                    long startNum = 0; //从0开始读
                    tf = openFile(f, headers, inode, startPos, startNum);
                } else {
                    boolean updated = tf.getLastUpdated() < f.lastModified();
                    if (updated) {
                        if (tf.getRaf() == null) { // 获取文件的读取手柄
                            tf = openFile(f, headers, inode, tf.getPos(), tf.getLineNum());
                        }
                        if (f.length() < tf.getPos()) { // 文件的长度小于上次读取的指针说明文件内容被删除了，改成从0读取
                            logger.info("Pos " + tf.getPos() + " is larger than file size! "
                                    + "Restarting from pos 0, file: " + tf.getPath() + ", inode: " + inode);
                            tf.updatePos(tf.getPath(), inode, 0, 0);
                        }
                    }
                    tf.setNeedTail(updated);
                }
                tailFiles.put(inode, tf);
                updatedInodes.add(inode);
            }
        }
        return updatedInodes;
    }

    public List<Long> updateTailFiles() throws IOException {
        return updateTailFiles(false);
    }


    private long getInode(File file) throws IOException {
        long inode = (long) Files.getAttribute(file.toPath(), "unix:ino");
        return inode;
    }

    private TailFile openFile(File file, Map<String, String> headers, long inode, long pos, long lineNum) {
        try {
            logger.info("Opening file: " + file + ", inode: " + inode + ", pos: " + pos + ", lineNum：" + lineNum);
            return new TailFile(file, headers, inode, pos, lineNum);
        } catch (IOException e) {
            throw new FlumeException("Failed opening file: " + file, e);
        }
    }

    /**
     * Special builder class for ReliableTaildirEventReader
     */
    public static class Builder {
        private Map<String, String> filePaths;
        private Map<String, String[]> exclusiveFiles;
        private long fileExpiredTime;
        private Table<String, String, String> headerTable;
        private String positionFilePath;
        private boolean skipToEnd;
        private boolean addByteOffset;
        private boolean cachePatternMatching;
        private Boolean annotateFileName = TaildirSourceConfigurationConstants.DEFAULT_FILE_HEADER;
        private String fileNameHeader = TaildirSourceConfigurationConstants.DEFAULT_FILENAME_HEADER_KEY;

        public Builder filePaths(Map<String, String> filePaths) {
            this.filePaths = filePaths;
            return this;
        }

        public Builder exclusiveFiles(Map<String, String[]> exclusiveFiles) {
            this.exclusiveFiles = exclusiveFiles;
            return this;
        }

        public Builder fileExpiredTime(long fileExpiredTime) {
            this.fileExpiredTime = fileExpiredTime;
            return this;
        }

        public Builder headerTable(Table<String, String, String> headerTable) {
            this.headerTable = headerTable;
            return this;
        }

        public Builder positionFilePath(String positionFilePath) {
            this.positionFilePath = positionFilePath;
            return this;
        }

        public Builder skipToEnd(boolean skipToEnd) {
            this.skipToEnd = skipToEnd;
            return this;
        }

        public Builder addByteOffset(boolean addByteOffset) {
            this.addByteOffset = addByteOffset;
            return this;
        }

        public Builder cachePatternMatching(boolean cachePatternMatching) {
            this.cachePatternMatching = cachePatternMatching;
            return this;
        }

        public Builder annotateFileName(boolean annotateFileName) {
            this.annotateFileName = annotateFileName;
            return this;
        }

        public Builder fileNameHeader(String fileNameHeader) {
            this.fileNameHeader = fileNameHeader;
            return this;
        }

        public ReliableTaildirEventReader build() throws IOException {
            return new ReliableTaildirEventReader(filePaths, exclusiveFiles, fileExpiredTime, headerTable, positionFilePath, skipToEnd,
                    addByteOffset, cachePatternMatching,
                    annotateFileName, fileNameHeader);
        }
    }

}
