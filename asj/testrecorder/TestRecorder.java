package asj.testrecorder;



import java.awt.PointerInfo;

import java.awt.MouseInfo;

import java.awt.image.ImageObserver;

import java.awt.Image;

import javax.sound.sampled.Line;

import javax.sound.sampled.AudioSystem;

import javax.sound.sampled.DataLine;

import javax.sound.sampled.TargetDataLine;

import javax.sound.sampled.LineUnavailableException;

import java.util.concurrent.TimeUnit;

import asj.testrecorder.exceptions.ASJTestRecorderException;
import asj.testrecorder.media.MovieWriter;
import asj.testrecorder.media.avi.AVIWriter;
import asj.testrecorder.media.color.Colors;
import asj.testrecorder.media.image.Images;
import asj.testrecorder.media.quicktime.QuickTimeWriter;

import java.awt.image.IndexColorModel;


import java.util.Date;

import java.text.SimpleDateFormat;

import java.io.File;

import java.awt.GraphicsConfiguration;


import java.util.Collections;

import java.util.LinkedList;

import java.awt.RenderingHints;


import java.awt.Frame;

import java.awt.Window;

import java.io.IOException;

import java.awt.AWTException;


import javax.sound.sampled.AudioFormat;

import java.awt.Point;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import java.awt.Graphics2D;

import java.util.List;

import java.awt.image.BufferedImage;

import java.awt.Rectangle;

import java.awt.Robot;




public class TestRecorder

{

    private CursorEnum cursor;

    private String format;

    private int depth;

    private MovieWriter w;

    private long startTime;

    private long time;

    private float screenRate;

    private float mouseRate;

    private int aviSyncInterval;

    private int qtSyncInterval;

    private long maxFrameDuration;

    private Robot robot;

    private Rectangle rect;

    private BufferedImage screenCapture;

    private List<MouseCapture> mouseCaptures;

    private BufferedImage videoImg;

    private Graphics2D videoGraphics;

    private ScheduledThreadPoolExecutor screenTimer;

    private ScheduledThreadPoolExecutor mouseTimer;

    private BufferedImage cursorImg;

    private Point cursorOffset;

    private final Object sync;

    private float audioRate;

    private Thread audioRunner;

    private AudioFormat audioFormat;

    private String recordingName;

    private String recordingRootDirectiry;

    

    public TestRecorder(final Boolean isAudioRecordingEnabled) throws ASJTestRecorderException {

        super();

        this.depth = 24;

        this.screenRate = 15.0f;

        this.mouseRate = 30.0f;

        this.aviSyncInterval = (int)(Math.max(this.screenRate, this.mouseRate) * 60.0f);

        this.qtSyncInterval = (int)Math.max(this.screenRate, this.mouseRate);

        this.maxFrameDuration = 1000L;

        this.cursorOffset = new Point(-8, -5);

        this.sync = new Object();

        this.recordingName = "";

        this.recordingRootDirectiry = "";

        try {

            this.recorder(this.recordingRootDirectiry, this.recordingName, isAudioRecordingEnabled);

        }

        catch (AWTException e) {

            throw new ASJTestRecorderException(e.getMessage());

        }

        catch (IOException e2) {

            throw new ASJTestRecorderException(e2.getMessage());

        }

    }

    

    public TestRecorder(final String recordingName, final Boolean isAudioRecordingEnabled) throws ASJTestRecorderException {

        super();

        this.depth = 24;

        this.screenRate = 15.0f;

        this.mouseRate = 30.0f;

        this.aviSyncInterval = (int)(Math.max(this.screenRate, this.mouseRate) * 60.0f);

        this.qtSyncInterval = (int)Math.max(this.screenRate, this.mouseRate);

        this.maxFrameDuration = 1000L;

        this.cursorOffset = new Point(-8, -5);

        this.sync = new Object();

        this.recordingName = "";

        this.recordingRootDirectiry = "";

        this.recordingName = recordingName;

        this.recordingRootDirectiry = "";

        try {

            this.recorder(this.recordingRootDirectiry, recordingName, isAudioRecordingEnabled);

        }

        catch (AWTException e) {

            throw new ASJTestRecorderException(e.getMessage());

        }

        catch (IOException e2) {

            throw new ASJTestRecorderException(e2.getMessage());

        }

    }

    

    public TestRecorder(final String recordingRootDirectiry, final String recordingName, final Boolean isAudioRecordingEnabled) throws ASJTestRecorderException {

        super();

        this.depth = 24;

        this.screenRate = 15.0f;

        this.mouseRate = 30.0f;

        this.aviSyncInterval = (int)(Math.max(this.screenRate, this.mouseRate) * 60.0f);

        this.qtSyncInterval = (int)Math.max(this.screenRate, this.mouseRate);

        this.maxFrameDuration = 1000L;

        this.cursorOffset = new Point(-8, -5);

        this.sync = new Object();

        this.recordingName = "";

        this.recordingRootDirectiry = "";

        this.recordingName = recordingName;

        this.recordingRootDirectiry = recordingRootDirectiry;

        try {

            this.recorder(recordingRootDirectiry, recordingName, isAudioRecordingEnabled);

        }

        catch (AWTException e) {

            throw new ASJTestRecorderException(e.getMessage());

        }

        catch (IOException e2) {

            throw new ASJTestRecorderException(e2.getMessage());

        }

    }

    

    private void recorder(final String recordingRootDirectiry, final String recordingName, final Boolean isAudioRecordingEnabled) throws IOException, AWTException {

        final Window window = new Window((Frame)null);

        final GraphicsConfiguration cfg = window.getGraphicsConfiguration();

        this.format = "QuickTime";

        this.depth = 24;

        this.cursor = CursorEnum.WHITE;

        this.screenRate = 15.0f;

        this.mouseRate = 30.0f;

        if (isAudioRecordingEnabled) {

            this.audioRate = 44100.0f;

        }

        else {

            this.audioRate = 0.0f;

        }

        this.aviSyncInterval = (int)(Math.max(this.screenRate, this.mouseRate) * 60.0f);

        this.qtSyncInterval = (int)Math.max(this.screenRate, this.mouseRate);

        this.rect = cfg.getBounds();

        this.robot = new Robot(cfg.getDevice());

        if (this.depth == 24) {

            this.videoImg = new BufferedImage(this.rect.width, this.rect.height, 1);

        }

        else if (this.depth == 16) {

            this.videoImg = new BufferedImage(this.rect.width, this.rect.height, 9);

        }

        else {

            if (this.depth != 8) {

                throw new IOException("Unsupported color depth " + this.depth);

            }

            this.videoImg = new BufferedImage(this.rect.width, this.rect.height, 13, Colors.createMacColors());

        }

        (this.videoGraphics = this.videoImg.createGraphics()).setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);

        this.videoGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

        this.videoGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        this.mouseCaptures = Collections.synchronizedList(new LinkedList<MouseCapture>());

        if (this.cursor == CursorEnum.BLACK) {

            this.cursorImg = Images.toBufferedImage(Images.createImage(TestRecorder.class, "/asj/testrecorder/media/images/Cursor.black.png"));

        }

        else {

            this.cursorImg = Images.toBufferedImage(Images.createImage(TestRecorder.class, "/asj/testrecorder/media/images/Cursor.white.png"));

        }

        this.createMovieWriter(recordingRootDirectiry, recordingName);

    }

    

    protected void createMovieWriter(String recordingRootDirectiry, String recordingName) throws IOException {

        if (recordingRootDirectiry == null) {

            recordingRootDirectiry = "";

        }

        final File recordingRootDir = new File(recordingRootDirectiry);

        if (recordingName == null) {

            recordingName = "";

        }

        File folder;

        if (recordingRootDirectiry.trim() == "") {

            folder = new File("." + File.separator);

        }

        else {

            if (!recordingRootDir.exists()) {

                throw new IOException("Directory \"" + recordingRootDir + "\" does not exist.");

            }

            if (!recordingRootDir.isDirectory()) {

                throw new IOException("\"" + recordingRootDir + "\" is not a directory.");

            }

            folder = new File(String.valueOf(recordingRootDirectiry) + File.separator);

        }

        if (!folder.exists()) {

            folder.mkdirs();

        }

        else if (!folder.isDirectory()) {

            throw new IOException("\"" + folder + "\" is not a directory.");

        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss");

        if (this.format.equals("AVI")) {

            final AVIWriter aviw = (AVIWriter)(this.w = new AVIWriter(new File(folder, "ScreenRecording " + dateFormat.format(new Date()) + ".avi")));

            aviw.addVideoTrack(AVIWriter.VIDEO_SCREEN_CAPTURE, 1L, (int)this.mouseRate, this.rect.width, this.rect.height, this.depth, this.aviSyncInterval);

            if (this.depth == 8) {

                aviw.setPalette(0, (IndexColorModel)this.videoImg.getColorModel());

            }

        }

        else {

            if (!this.format.equals("QuickTime")) {

                throw new IOException("Unsupported format " + this.format);

            }

            final QuickTimeWriter qtw = (QuickTimeWriter)(this.w = new QuickTimeWriter(new File(folder, String.valueOf(recordingName) + ".mov")));

            qtw.addVideoTrack(QuickTimeWriter.VIDEO_ANIMATION, 1000L, this.rect.width, this.rect.height, this.depth, this.qtSyncInterval);

            if (this.audioRate > 0.0f) {

                qtw.addAudioTrack(this.audioFormat = new AudioFormat(this.audioRate, 16, 1, true, true));

            }

            if (this.depth == 8) {

                qtw.setVideoColorTable(0, (IndexColorModel)this.videoImg.getColorModel());

            }

        }

    }

    

    public void start() throws ASJTestRecorderException {

        final long currentTimeMillis = System.currentTimeMillis();

        this.time = currentTimeMillis;

        this.startTime = currentTimeMillis;

        (this.screenTimer = new ScheduledThreadPoolExecutor(1)).scheduleAtFixedRate(new Runnable() {

            @Override

            public void run() {

                try {

                    TestRecorder.this.grabScreen();

                }

                catch (IOException ex) {

                    ex.printStackTrace();

                }

            }

        }, (int)(1000.0f / this.screenRate), (int)(1000.0f / this.screenRate), TimeUnit.MILLISECONDS);

        (this.mouseTimer = new ScheduledThreadPoolExecutor(1)).scheduleAtFixedRate(new Runnable() {

            @Override

            public void run() {

                TestRecorder.this.grabMouse();

            }

        }, (int)(1000.0f / this.mouseRate), (int)(1000.0f / this.mouseRate), TimeUnit.MILLISECONDS);

        if (this.audioRate > 0.0f && this.w instanceof QuickTimeWriter) {

            try {

                this.startAudio();

            }

            catch (LineUnavailableException e) {

                throw new ASJTestRecorderException(e.getMessage());

            }

        }

    }

    

    private void startAudio() throws LineUnavailableException {

        final DataLine.Info info = new DataLine.Info(TargetDataLine.class, this.audioFormat);

        final TargetDataLine line = (TargetDataLine)AudioSystem.getLine(info);

        line.open(this.audioFormat);

        line.start();

        final int bufferSize;

        if (this.audioFormat.getFrameSize() != -1) {

            bufferSize = (int)this.audioFormat.getSampleRate() * this.audioFormat.getFrameSize();

        }

        else {

            bufferSize = (int)this.audioFormat.getSampleRate();

        }

        (this.audioRunner = new Thread() {

            @Override

            public void run() {

                final byte[] buffer = new byte[bufferSize];

                try {

                    while (TestRecorder.this.audioRunner == this) {

                        final int count = line.read(buffer, 0, buffer.length);

                        if (count > 0) {

                            synchronized (TestRecorder.this.sync) {

                                final int sampleCount = count * 8 / TestRecorder.this.audioFormat.getSampleSizeInBits();

                                TestRecorder.this.w.writeSamples(1, sampleCount, buffer, 0, count, 1L, true);

                            }

                            

                        }

                    }

                }

                catch (IOException e) {

                    e.printStackTrace();

                    return;

                }

                finally {

                    line.close();

                }

                line.close();

            }

        }).start();

    }

    

    public void stop() throws ASJTestRecorderException {

        try {

            Thread.sleep(2000L);

        }

        catch (InterruptedException e1) {

            throw new ASJTestRecorderException(e1.getMessage());

        }

        this.mouseTimer.shutdown();

        this.screenTimer.shutdown();

        final Thread T = this.audioRunner;

        this.audioRunner = null;

        try {

            this.mouseTimer.awaitTermination((int)(1000.0f / this.mouseRate), TimeUnit.MILLISECONDS);

            this.screenTimer.awaitTermination((int)(1000.0f / this.screenRate), TimeUnit.MILLISECONDS);

            if (T != null) {

                T.join();

            }

        }

        catch (InterruptedException e2) {

            throw new ASJTestRecorderException(e2.getMessage());

        }

        synchronized (this.sync) {

            try {

                this.w.close();

            }

            catch (IOException e3) {

                throw new ASJTestRecorderException(e3.getMessage());

            }

            this.w = null;

        }

        // monitorexit(this.sync)

        this.videoGraphics.dispose();

        this.videoImg.flush();

    }

    

    private void grabScreen() throws IOException {

        this.screenCapture = this.robot.createScreenCapture(new Rectangle(0, 0, this.rect.width, this.rect.height));

        final long now = System.currentTimeMillis();

        this.videoGraphics.drawImage(this.screenCapture, 0, 0, null);

        boolean hasMouseCapture = false;

        if (this.cursor != CursorEnum.NONE) {

            final Point previous = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

            while (!this.mouseCaptures.isEmpty() && this.mouseCaptures.get(0).time < now) {

                final MouseCapture pc = this.mouseCaptures.remove(0);

                if (pc.time > this.time) {

                    hasMouseCapture = true;

                    final Point p3;

                    final Point p = p3 = pc.p;

                    p3.x -= this.rect.x;

                    final Point point = p;

                    point.y -= this.rect.y;

                    synchronized (this.sync) {

                        if (!this.w.isVFRSupported() || p.x != previous.x || p.y != previous.y || pc.time - this.time > this.maxFrameDuration) {

                            previous.x = p.x;

                            previous.y = p.y;

                            this.videoGraphics.drawImage(this.cursorImg, p.x + this.cursorOffset.x, p.y + this.cursorOffset.y, null);

                            if (this.w == null) {

                                // monitorexit(this.sync)

                                return;

                            }

                            try {

                                this.w.writeFrame(0, this.videoImg, (int)(pc.time - this.time));

                            }

                            catch (Throwable t) {

                                throw new IllegalStateException("TestRecorder Error");

                            }

                            this.time = pc.time;

                            this.videoGraphics.drawImage(this.screenCapture, p.x + this.cursorOffset.x, p.y + this.cursorOffset.y, p.x + this.cursorOffset.x + this.cursorImg.getWidth() - 1, p.y + this.cursorOffset.y + this.cursorImg.getHeight() - 1, p.x + this.cursorOffset.x, p.y + this.cursorOffset.y, p.x + this.cursorOffset.x + this.cursorImg.getWidth() - 1, p.y + this.cursorOffset.y + this.cursorImg.getHeight() - 1, null);

                        }

                    }

                    // monitorexit(this.sync)

                }

            }

        }

        if (!hasMouseCapture) {

            if (this.cursor != CursorEnum.NONE) {

                final PointerInfo info = MouseInfo.getPointerInfo();

                final Point p2 = info.getLocation();

                this.videoGraphics.drawImage(this.cursorImg, p2.x + this.cursorOffset.x, p2.x + this.cursorOffset.y, null);

            }

            synchronized (this.sync) {

                this.w.writeFrame(0, this.videoImg, (int)(now - this.time));

            }

            // monitorexit(this.sync)

            this.time = now;

        }

    }

    

    private void grabMouse() {

        final long now = System.currentTimeMillis();

        final PointerInfo info = MouseInfo.getPointerInfo();

        this.mouseCaptures.add(new MouseCapture(now, info.getLocation()));

    }

    

    private enum CursorEnum

    {

        BLACK, 

        WHITE, 

        NONE;

    }

    

    private static class MouseCapture

    {

        public long time;

        public Point p;

        

        public MouseCapture(final long time, final Point p) {

            super();

            this.time = time;

            this.p = p;

        }

    }
        
}