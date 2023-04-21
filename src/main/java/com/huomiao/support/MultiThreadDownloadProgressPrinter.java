package com.huomiao.support;

import java.util.Arrays;
import java.util.Objects;

public class MultiThreadDownloadProgressPrinter implements DownloadProgressPrinter {
    private PartProgress[] partProgresses;
    private long contentLength;

    public MultiThreadDownloadProgressPrinter(int threadNum) {
        this.partProgresses = new PartProgress[threadNum];
    }

    @Override
    public void print(String indexText, long contentLength, long alreadyDownloadLength, long speed) {
        PartProgress partProgress = this.getPartProgress(indexText);
        partProgress.setAlreadyDownloadLength(alreadyDownloadLength);
    }

    private PartProgress getPartProgress(String indexText) {
        PartProgress partProgress = this.partProgresses[Integer.valueOf(indexText)];
        if (Objects.nonNull(partProgress)) {
            return partProgress;
        }
        partProgress = new PartProgress();
        this.partProgresses[Integer.valueOf(indexText)] = partProgress;
        return partProgress;
    }

    @Override
    public long setContentLength(long contentLength) {
        return this.contentLength = contentLength;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public long getAlreadyDownloadLength() {
        return Arrays.stream(partProgresses)
                .filter(Objects::nonNull)
                .map(PartProgress::getAlreadyDownloadLength)
                .reduce(Long::sum)
                .orElse(0L);
    }


    static class PartProgress {
        long alreadyDownloadLength;

        public void setAlreadyDownloadLength(long alreadyDownloadLength) {
            this.alreadyDownloadLength = alreadyDownloadLength;
        }

        public long getAlreadyDownloadLength() {
            return alreadyDownloadLength;
        }
    }

}
