package com.crowdin.cli.utils.console;

import com.crowdin.cli.commands.Outputter;
import com.crowdin.cli.utils.Utils;

import java.util.concurrent.locks.ReentrantLock;

class Spinner extends Thread {

    private static final int SPINNER_INTERVAL = 250;

    private static final ReentrantLock lock = new ReentrantLock();

    private Outputter out;
    private int counter;
    private boolean isSpin;
    private String message;
    private boolean noProgress;

    Spinner(Outputter out, String message, boolean noProgress) {
        this();
        this.out = out;
        this.message = message;
        this.noProgress = noProgress;
        isSpin = true;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private final String[] unixFrames = new String[]{
        "[●∙∙∙∙] ",
        "[∙●∙∙∙] ",
        "[∙∙●∙∙] ",
        "[∙∙∙●∙] ",
        "[∙∙∙∙●] ",
    };

    private final String[] windowsFrames = new String[]{
        "[ \\ ] ",
        "[ | ] ",
        "[ / ] ",
        "[ - ] "
    };

    private Spinner() {
    }

    @Override
    public void run() {
        if (noProgress) {
            return;
        }

        lock.lock();
        try {
            String[] frames = getFrames();
            while (isSpin) {
                String frame = frames[counter++ % frames.length] + message;
                out.print(frame);
                try {
                    Thread.sleep(SPINNER_INTERVAL);
                } catch (InterruptedException e) { /*ignore*/ }
                clearFrame(frame);
            }
        } finally {
            lock.unlock();
        }
    }

    void stopSpinning(ExecutionStatus status) {
        this.stopSpinning(status, this.message);
    }

    void stopSpinning(ExecutionStatus status, String stopMessage) {
        isSpin = false;
        lock.lock();
        try {
            out.println(status.withIcon(stopMessage));
        } finally {
            lock.unlock();
        }
    }

    private String[] getFrames() {
        return Utils.isWindows() ? windowsFrames : unixFrames;
    }

    private void clearFrame(String frame) {
        int bound = frame.length();
        for (int value = 0; value < bound; value++) {
            out.print("\b \b");
        }
    }
}