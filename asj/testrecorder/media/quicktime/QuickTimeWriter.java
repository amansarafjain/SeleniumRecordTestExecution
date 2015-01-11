/*    1:     */ package asj.testrecorder.media.quicktime;
/*    2:     */ 
/*    3:     */ import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Codec;
import asj.testrecorder.media.MovieWriter;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.io.ImageOutputStreamAdapter;
import asj.testrecorder.media.jpeg.JPEGCodec;
import asj.testrecorder.media.png.PNGCodec;

/*   10:     */ import java.awt.image.BufferedImage;
/*   11:     */ import java.awt.image.IndexColorModel;
/*   12:     */ import java.io.ByteArrayOutputStream;
/*   13:     */ import java.io.File;
/*   14:     */ import java.io.FileInputStream;
/*   15:     */ import java.io.IOException;
/*   16:     */ import java.io.InputStream;
/*   17:     */ import java.io.OutputStream;
/*   18:     */ import java.io.PrintStream;
/*   19:     */ import java.util.ArrayList;
/*   20:     */ import java.util.Date;
/*   21:     */ import java.util.zip.DeflaterOutputStream;
/*   22:     */ import javax.imageio.stream.FileImageOutputStream;
/*   23:     */ import javax.imageio.stream.ImageOutputStream;
/*   24:     */ import javax.imageio.stream.MemoryCacheImageOutputStream;
/*   25:     */ import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
/*   27:     */ 
/*   28:     */ public class QuickTimeWriter
/*   29:     */   extends AbstractQuickTimeStream
/*   30:     */   implements MovieWriter
/*   31:     */ {
/*   32: 131 */   public static final VideoFormat VIDEO_RAW = new VideoFormat("raw ", "NONE");
/*   33: 132 */   public static final VideoFormat VIDEO_ANIMATION = new VideoFormat("rle ", "Animation");
/*   34: 133 */   public static final VideoFormat VIDEO_JPEG = new VideoFormat("jpeg", "Photo - JPEG");
/*   35: 134 */   public static final VideoFormat VIDEO_PNG = new VideoFormat("png ", "PNG");
/*   36:     */   
/*   37:     */   public QuickTimeWriter(File file)
/*   38:     */     throws IOException
/*   39:     */   {
/*   40: 142 */     if (file.exists()) {
/*   41: 143 */       file.delete();
/*   42:     */     }
/*   43: 145 */     this.out = new FileImageOutputStream(file);
/*   44: 146 */     this.streamOffset = 0L;
/*   45:     */   }
/*   46:     */   
/*   47:     */   public QuickTimeWriter(ImageOutputStream out)
/*   48:     */     throws IOException
/*   49:     */   {
/*   50: 155 */     this.out = out;
/*   51: 156 */     this.streamOffset = out.getStreamPosition();
/*   52:     */   }
/*   53:     */   
/*   54:     */   public void setMovieTimeScale(long timeScale)
/*   55:     */   {
/*   56: 168 */     if ((timeScale < 1L) || (timeScale > 8589934592L)) {
/*   57: 169 */       throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
/*   58:     */     }
/*   59: 171 */     this.movieTimeScale = timeScale;
/*   60:     */   }
/*   61:     */   
/*   62:     */   public long getMovieTimeScale()
/*   63:     */   {
/*   64: 181 */     return this.movieTimeScale;
/*   65:     */   }
/*   66:     */   
/*   67:     */   public long getMediaTimeScale(int track)
/*   68:     */   {
/*   69: 192 */     return ((AbstractQuickTimeStream.Track)this.tracks.get(track)).mediaTimeScale;
/*   70:     */   }
/*   71:     */   
/*   72:     */   public long getMediaDuration(int track)
/*   73:     */   {
/*   74: 202 */     return ((AbstractQuickTimeStream.Track)this.tracks.get(track)).mediaDuration;
/*   75:     */   }
/*   76:     */   
/*   77:     */   public long getUneditedTrackDuration(int track)
/*   78:     */   {
/*   79: 216 */     AbstractQuickTimeStream.Track t = (AbstractQuickTimeStream.Track)this.tracks.get(track);
/*   80: 217 */     return t.mediaDuration * t.mediaTimeScale / this.movieTimeScale;
/*   81:     */   }
/*   82:     */   
/*   83:     */   public long getTrackDuration(int track)
/*   84:     */   {
/*   85: 233 */     return ((AbstractQuickTimeStream.Track)this.tracks.get(track)).getTrackDuration(this.movieTimeScale);
/*   86:     */   }
/*   87:     */   
/*   88:     */   public long getMovieDuration()
/*   89:     */   {
/*   90: 242 */     long duration = 0L;
/*   91: 243 */     for (AbstractQuickTimeStream.Track t : this.tracks) {
/*   92: 244 */       duration = Math.max(duration, t.getTrackDuration(this.movieTimeScale));
/*   93:     */     }
/*   94: 246 */     return duration;
/*   95:     */   }
/*   96:     */   
/*   97:     */   public void setVideoColorTable(int track, IndexColorModel icm)
/*   98:     */   {
/*   99: 256 */     AbstractQuickTimeStream.VideoTrack t = (AbstractQuickTimeStream.VideoTrack)this.tracks.get(track);
/*  100: 257 */     t.videoColorTable = icm;
/*  101:     */   }
/*  102:     */   
/*  103:     */   public IndexColorModel getVideoColorTable(int track)
/*  104:     */   {
/*  105: 268 */     AbstractQuickTimeStream.VideoTrack t = (AbstractQuickTimeStream.VideoTrack)this.tracks.get(track);
/*  106: 269 */     return t.videoColorTable;
/*  107:     */   }
/*  108:     */   
/*  109:     */   public void setEditList(int track, AbstractQuickTimeStream.Edit[] editList)
/*  110:     */   {
/*  111: 280 */     if ((editList != null) && (editList.length > 0) && (editList[(editList.length - 1)].mediaTime == -1)) {
/*  112: 281 */       throw new IllegalArgumentException("Edit list must not end with empty edit.");
/*  113:     */     }
/*  114: 283 */     ((AbstractQuickTimeStream.Track)this.tracks.get(track)).editList = editList;
/*  115:     */   }
/*  116:     */   
/*  117:     */   public int addVideoTrack(VideoFormat format, long timeScale, int width, int height)
/*  118:     */     throws IOException
/*  119:     */   {
/*  120: 304 */     return addVideoTrack(format.getEncoding(), format.getCompressorName(), timeScale, width, height, 24, 30);
/*  121:     */   }
/*  122:     */   
/*  123:     */   public int addVideoTrack(VideoFormat format, long timeScale, int width, int height, int depth, int syncInterval)
/*  124:     */     throws IOException
/*  125:     */   {
/*  126: 325 */     return addVideoTrack(format.getEncoding(), format.getCompressorName(), timeScale, width, height, depth, syncInterval);
/*  127:     */   }
/*  128:     */   
/*  129:     */   public int addVideoTrack(String compressionType, String compressorName, long timeScale, int width, int height, int depth, int syncInterval)
/*  130:     */     throws IOException
/*  131:     */   {
/*  132: 350 */     ensureStarted();
/*  133: 351 */     if ((compressionType == null) || (compressionType.length() != 4)) {
/*  134: 352 */       throw new IllegalArgumentException("compressionType must be 4 characters long:" + compressionType);
/*  135:     */     }
/*  136: 354 */     if ((compressorName == null) || (compressorName.length() < 1) || (compressorName.length() > 32)) {
/*  137: 355 */       throw new IllegalArgumentException("compressorName must be between 1 and 32 characters long:" + compressionType);
/*  138:     */     }
/*  139: 357 */     if ((timeScale < 1L) || (timeScale > 8589934592L)) {
/*  140: 358 */       throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
/*  141:     */     }
/*  142: 360 */     if ((width < 1) || (height < 1)) {
/*  143: 361 */       throw new IllegalArgumentException("Width and height must be greater than 0, width:" + width + " height:" + height);
/*  144:     */     }
/*  145: 364 */     AbstractQuickTimeStream.VideoTrack t = new AbstractQuickTimeStream.VideoTrack();
/*  146: 365 */     t.mediaCompressionType = compressionType;
/*  147: 366 */     t.mediaCompressorName = compressorName;
/*  148: 367 */     t.mediaTimeScale = timeScale;
/*  149: 368 */     t.videoWidth = width;
/*  150: 369 */     t.videoHeight = height;
/*  151: 370 */     t.videoDepth = depth;
/*  152: 371 */     t.syncInterval = syncInterval;
/*  153: 372 */     t.videoFormat = new VideoFormat(compressionType, compressorName, Byte[].class, width, height, depth);
/*  154: 373 */     createCodec(t);
/*  155: 374 */     this.tracks.add(t);
/*  156: 375 */     return this.tracks.size() - 1;
/*  157:     */   }
/*  158:     */   
/*  159:     */   private void createCodec(AbstractQuickTimeStream.VideoTrack vt)
/*  160:     */   {
/*  161: 379 */     String enc = vt.videoFormat.getEncoding();
/*  162: 380 */     if (enc.equals("jpeg")) {
/*  163: 381 */       vt.codec = new JPEGCodec();
/*  164: 382 */     } else if (enc.equals("png ")) {
/*  165: 383 */       vt.codec = new PNGCodec();
/*  166: 384 */     } else if (enc.equals("raw ")) {
/*  167: 385 */       vt.codec = new RawCodec();
/*  168: 386 */     } else if (enc.equals("rle ")) {
/*  169: 387 */       vt.codec = new AnimationCodec();
/*  170:     */     }
/*  171: 389 */     vt.codec.setInputFormat(new VideoFormat("image", BufferedImage.class, vt.videoWidth, vt.videoHeight, vt.videoDepth));
/*  172: 390 */     vt.codec.setOutputFormat(new VideoFormat(vt.videoFormat.getEncoding(), vt.videoFormat.getCompressorName(), Byte[].class, vt.videoWidth, vt.videoHeight, vt.videoDepth));
/*  173: 391 */     vt.codec.setQuality(vt.videoQuality);
/*  174:     */   }
/*  175:     */   
/*  176:     */   public Codec getCodec(int track)
/*  177:     */   {
/*  178: 396 */     return ((AbstractQuickTimeStream.Track)this.tracks.get(track)).codec;
/*  179:     */   }
/*  180:     */   
/*  181:     */   public void setCodec(int track, Codec codec)
/*  182:     */   {
/*  183: 401 */     ((AbstractQuickTimeStream.Track)this.tracks.get(track)).codec = codec;
/*  184:     */   }
/*  185:     */   
/*  186:     */   public int addAudioTrack(AudioFormat format)
/*  187:     */     throws IOException
/*  188:     */   {
/*  189: 414 */     ensureStarted();
/*  190:     */     
/*  191: 416 */     double sampleRate = format.getSampleRate();
/*  192: 417 */     long timeScale = (int)Math.floor(sampleRate);
/*  193: 418 */     int sampleSizeInBits = format.getSampleSizeInBits();
/*  194: 419 */     int numberOfChannels = format.getChannels();
/*  195: 420 */     boolean bigEndian = format.isBigEndian();
/*  196: 421 */     int frameDuration = (int)(format.getSampleRate() / format.getFrameRate());
/*  197: 422 */     int frameSize = format.getFrameSize();
/*  198: 423 */     boolean isCompressed = (format.getProperty("vbr") != null) && (((Boolean)format.getProperty("vbr")).booleanValue());
/*  199:     */     String qtAudioFormat;
/*  200: 425 */     if (format.getEncoding().equals(AudioFormat.Encoding.ALAW))
/*  201:     */     {
/*  202: 426 */        qtAudioFormat = "alaw";
/*  203: 427 */       if (sampleSizeInBits != 8) {
/*  204: 428 */         throw new IllegalArgumentException("Sample size of 8 for ALAW required:" + sampleSizeInBits);
/*  205:     */       }
/*  206:     */     }
/*  207: 430 */     else if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
/*  208:     */     {

/*  213: 431 */       switch (sampleSizeInBits)
/*  214:     */       {
/*  215:     */       case 16: 
/*  216: 433 */         qtAudioFormat = bigEndian ? "twos" : "sowt";
/*  217: 434 */         break;
/*  218:     */       case 24: 
/*  219: 436 */         qtAudioFormat = "in24";
/*  220: 437 */         break;
/*  221:     */       case 32: 
/*  222: 439 */         qtAudioFormat = "in32";
/*  223: 440 */         break;
/*  224:     */       default: 
/*  225: 442 */         throw new IllegalArgumentException("Sample size of 16, 24 or 32 for PCM_SIGNED required:" + sampleSizeInBits);
/*  226:     */       }
/*  227:     */     }
/*  228:     */     else
/*  229:     */     {
/*  230:     */       
/*  231: 444 */       if (format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED))
/*  232:     */       {
/*  233: 445 */         if (sampleSizeInBits != 8) {
/*  234: 446 */           throw new IllegalArgumentException("Sample size of 8 PCM_UNSIGNED required:" + sampleSizeInBits);
/*  235:     */         }
/*  236: 448 */         qtAudioFormat = "raw ";
/*  237:     */       }
/*  238:     */       else
/*  239:     */       {
/*  240:     */       
/*  241: 449 */         if (format.getEncoding().equals(AudioFormat.Encoding.ULAW))
/*  242:     */         {
/*  243: 450 */           if (sampleSizeInBits != 8) {
/*  244: 451 */             throw new IllegalArgumentException("Sample size of 8 for ULAW required:" + sampleSizeInBits);
/*  245:     */           }
/*  246: 453 */           qtAudioFormat = "ulaw";
/*  247:     */         }
/*  248:     */         else
/*  249:     */         {
/*  250:     */           
/*  251: 454 */           if (format.getEncoding().toString().equals("MP3"))
/*  252:     */           {
/*  253: 455 */             qtAudioFormat = ".mp3";
/*  254:     */           }
/*  255:     */           else
/*  256:     */           {
/*  257: 457 */             qtAudioFormat = format.getEncoding().toString();
/*  258: 458 */             if (qtAudioFormat.length() != 4) {
/*  259: 459 */               throw new IllegalArgumentException("Unsupported encoding:" + format.getEncoding());
/*  260:     */             }
/*  261:     */           }
/*  262:     */         }
/*  263:     */       }
/*  264:     */     }
/*  265: 463 */     return addAudioTrack(qtAudioFormat, timeScale, sampleRate, 
/*  266: 464 */       numberOfChannels, sampleSizeInBits, 
/*  267: 465 */       isCompressed, frameDuration, frameSize);
/*  268:     */   }
/*  269:     */   
/*  270:     */   public int addAudioTrack(String compressionType, long timeScale, double sampleRate, int numberOfChannels, int sampleSizeInBits, boolean isCompressed, int frameDuration, int frameSize)
/*  271:     */     throws IOException
/*  272:     */   {
/*  273: 495 */     ensureStarted();
/*  274: 496 */     if ((compressionType == null) || (compressionType.length() != 4)) {
/*  275: 497 */       throw new IllegalArgumentException("audioFormat must be 4 characters long:" + compressionType);
/*  276:     */     }
/*  277: 499 */     if ((timeScale < 1L) || (timeScale > 8589934592L)) {
/*  278: 500 */       throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
/*  279:     */     }
/*  280: 502 */     if (timeScale != (int)Math.floor(sampleRate)) {
/*  281: 503 */       throw new IllegalArgumentException("timeScale: " + timeScale + " must match integer portion of sampleRate: " + sampleRate);
/*  282:     */     }
/*  283: 505 */     if ((numberOfChannels != 1) && (numberOfChannels != 2)) {
/*  284: 506 */       throw new IllegalArgumentException("numberOfChannels must be 1 or 2: " + numberOfChannels);
/*  285:     */     }
/*  286: 508 */     if ((sampleSizeInBits != 8) && (sampleSizeInBits != 16)) {
/*  287: 509 */       throw new IllegalArgumentException("sampleSize must be 8 or 16: " + numberOfChannels);
/*  288:     */     }
/*  289: 512 */     AbstractQuickTimeStream.AudioTrack t = new AbstractQuickTimeStream.AudioTrack();
/*  290: 513 */     t.mediaCompressionType = compressionType;
/*  291: 514 */     t.mediaTimeScale = timeScale;
/*  292: 515 */     t.soundSampleRate = sampleRate;
/*  293: 516 */     t.soundCompressionId = (isCompressed ? -2 : -1);
/*  294: 517 */     t.soundNumberOfChannels = numberOfChannels;
/*  295: 518 */     t.soundSampleSize = sampleSizeInBits;
/*  296: 519 */     t.soundSamplesPerPacket = frameDuration;
/*  297: 520 */     if (isCompressed)
/*  298:     */     {
/*  299: 521 */       t.soundBytesPerPacket = frameSize;
/*  300: 522 */       t.soundBytesPerFrame = (frameSize * numberOfChannels);
/*  301:     */     }
/*  302:     */     else
/*  303:     */     {
/*  304: 524 */       t.soundBytesPerPacket = (frameSize / numberOfChannels);
/*  305: 525 */       t.soundBytesPerFrame = frameSize;
/*  306:     */     }
/*  307: 527 */     t.soundBytesPerSample = (sampleSizeInBits / 8);
/*  308: 528 */     this.tracks.add(t);
/*  309: 529 */     return this.tracks.size() - 1;
/*  310:     */   }
/*  311:     */   
/*  312:     */   public void setCompressionQuality(int track, float newValue)
/*  313:     */   {
/*  314: 550 */     AbstractQuickTimeStream.VideoTrack vt = (AbstractQuickTimeStream.VideoTrack)this.tracks.get(track);
/*  315: 551 */     vt.videoQuality = newValue;
/*  316: 552 */     if (vt.codec != null) {
/*  317: 553 */       vt.codec.setQuality(newValue);
/*  318:     */     }
/*  319:     */   }
/*  320:     */   
/*  321:     */   public float getCompressionQuality(int track)
/*  322:     */   {
/*  323: 563 */     return ((AbstractQuickTimeStream.VideoTrack)this.tracks.get(track)).videoQuality;
/*  324:     */   }
/*  325:     */   
/*  326:     */   public void setSyncInterval(int track, int i)
/*  327:     */   {
/*  328: 574 */     ((AbstractQuickTimeStream.VideoTrack)this.tracks.get(track)).syncInterval = i;
/*  329:     */   }
/*  330:     */   
/*  331:     */   public int getSyncInterval(int track)
/*  332:     */   {
/*  333: 579 */     return ((AbstractQuickTimeStream.VideoTrack)this.tracks.get(track)).syncInterval;
/*  334:     */   }
/*  335:     */   
/*  336:     */   protected void ensureStarted()
/*  337:     */     throws IOException
/*  338:     */   {
/*  339: 589 */     ensureOpen();
/*  340: 590 */     if (this.state == AbstractQuickTimeStream.States.FINISHED) {
/*  341: 591 */       throw new IOException("Can not write into finished movie.");
/*  342:     */     }
/*  343: 593 */     if (this.state != AbstractQuickTimeStream.States.STARTED)
/*  344:     */     {
/*  345: 594 */       this.creationTime = new Date();
/*  346: 595 */       writeProlog();
/*  347: 596 */       this.mdatAtom = new AbstractQuickTimeStream.WideDataAtom("mdat");
/*  348: 597 */       this.state = AbstractQuickTimeStream.States.STARTED;
/*  349:     */     }
/*  350:     */   }
/*  351:     */   
/*  352:     */   public void writeFrame(int track, BufferedImage image, long duration)
/*  353:     */     throws IOException
/*  354:     */   {
/*  355: 617 */     if (duration <= 0L) {
/*  356: 618 */       throw new IllegalArgumentException("Duration must be greater 0.");
/*  357:     */     }
/*  358: 620 */     AbstractQuickTimeStream.VideoTrack vt = (AbstractQuickTimeStream.VideoTrack)this.tracks.get(track);
/*  359: 621 */     if (vt.mediaType != AbstractQuickTimeStream.MediaType.VIDEO) {
/*  360: 622 */       throw new IllegalArgumentException("Track " + track + " is not a video track");
/*  361:     */     }
/*  362: 624 */     if (vt.codec == null) {
/*  363: 625 */       throw new UnsupportedOperationException("No codec for this video format.");
/*  364:     */     }
/*  365: 627 */     ensureStarted();
/*  366: 630 */     if (vt.videoWidth == -1)
/*  367:     */     {
/*  368: 631 */       vt.videoWidth = image.getWidth();
/*  369: 632 */       vt.videoHeight = image.getHeight();
/*  370:     */     }
/*  371: 635 */     else if ((vt.videoWidth != image.getWidth()) || (vt.videoHeight != image.getHeight()))
/*  372:     */     {
/*  373: 636 */       throw new IllegalArgumentException("Dimensions of frame[" + ((AbstractQuickTimeStream.Track)this.tracks.get(track)).getSampleCount() + 
/*  374: 637 */         "] (width=" + image.getWidth() + ", height=" + image.getHeight() + 
/*  375: 638 */         ") differs from video dimension (width=" + 
/*  376: 639 */         vt.videoWidth + ", height=" + vt.videoHeight + ") in track " + track + ".");
/*  377:     */     }
/*  378: 646 */     if (vt.outputBuffer == null) {
/*  379: 647 */       vt.outputBuffer = new Buffer();
/*  380:     */     }
/*  381: 650 */     boolean isSync = vt.syncInterval != 0;
/*  382:     */     
/*  383: 652 */     Buffer inputBuffer = new Buffer();
/*  384: 653 */     inputBuffer.flags = (isSync ? 16 : 0);
/*  385: 654 */     inputBuffer.data = image;
/*  386: 655 */     vt.codec.process(inputBuffer, vt.outputBuffer);
/*  387: 656 */     if (vt.outputBuffer.flags == 2) {
/*  388: 657 */       return;
/*  389:     */     }
/*  390: 660 */     isSync = (vt.outputBuffer.flags & 0x10) != 0;
/*  391:     */     
/*  392: 662 */     long offset = getRelativeStreamPosition();
/*  393: 663 */     OutputStream mdatOut = this.mdatAtom.getOutputStream();
/*  394: 664 */     mdatOut.write((byte[])vt.outputBuffer.data, vt.outputBuffer.offset, vt.outputBuffer.length);
/*  395:     */     
/*  396: 666 */     long length = getRelativeStreamPosition() - offset;
/*  397: 667 */     vt.addSample(new AbstractQuickTimeStream.Sample(duration, offset, length), 1, isSync);
/*  398:     */   }
/*  399:     */   
/*  400:     */   public void writeSample(int track, File file, long duration)
/*  401:     */     throws IOException
/*  402:     */   {
/*  403: 688 */     writeSample(track, file, duration, true);
/*  404:     */   }
/*  405:     */   
/*  406:     */   public void writeSample(int track, File file, long duration, boolean isSync)
/*  407:     */     throws IOException
/*  408:     */   {
/*  409: 709 */     ensureStarted();
/*  410: 710 */     FileInputStream in = null;
/*  411:     */     try
/*  412:     */     {
/*  413: 712 */       in = new FileInputStream(file);
/*  414: 713 */       writeSample(track, in, duration, isSync);
/*  415:     */     }
/*  416:     */     finally
/*  417:     */     {
/*  418: 715 */       if (in != null) {
/*  419: 716 */         in.close();
/*  420:     */       }
/*  421:     */     }
/*  422:     */   }
/*  423:     */   
/*  424:     */   public void writeSample(int track, InputStream in, long duration)
/*  425:     */     throws IOException
/*  426:     */   {
/*  427: 735 */     writeSample(track, in, duration, true);
/*  428:     */   }
/*  429:     */   
/*  430:     */   public void writeSample(int track, InputStream in, long duration, boolean isSync)
/*  431:     */     throws IOException
/*  432:     */   {
/*  433: 753 */     ensureStarted();
/*  434: 754 */     if (duration <= 0L) {
/*  435: 755 */       throw new IllegalArgumentException("duration must be greater 0");
/*  436:     */     }
/*  437: 757 */     AbstractQuickTimeStream.Track t = (AbstractQuickTimeStream.Track)this.tracks.get(track);
/*  438: 758 */     ensureOpen();
/*  439: 759 */     ensureStarted();
/*  440: 760 */     long offset = getRelativeStreamPosition();
/*  441: 761 */     OutputStream mdatOut = this.mdatAtom.getOutputStream();
/*  442: 762 */     byte[] buf = new byte[4096];
/*  443:     */     int len;
/*  444: 764 */     while ((len = in.read(buf)) != -1)
/*  445:     */     {
/*  446:     */      
/*  447: 765 */       mdatOut.write(buf, 0, len);
/*  448:     */     }
/*  449: 767 */     long length = getRelativeStreamPosition() - offset;
/*  450: 768 */     t.addSample(new AbstractQuickTimeStream.Sample(duration, offset, length), 1, isSync);
/*  451:     */   }
/*  452:     */   
/*  453:     */   public void writeSample(int track, byte[] data, long duration)
/*  454:     */     throws IOException
/*  455:     */   {
/*  456: 785 */     writeSample(track, data, 0, data.length, duration, true);
/*  457:     */   }
/*  458:     */   
/*  459:     */   public void writeSample(int track, byte[] data, long duration, boolean isSync)
/*  460:     */     throws IOException
/*  461:     */   {
/*  462: 803 */     ensureStarted();
/*  463: 804 */     writeSample(track, data, 0, data.length, duration, isSync);
/*  464:     */   }
/*  465:     */   
/*  466:     */   public void writeSample(int track, byte[] data, int off, int len, long duration)
/*  467:     */     throws IOException
/*  468:     */   {
/*  469: 823 */     ensureStarted();
/*  470: 824 */     writeSample(track, data, off, len, duration, true);
/*  471:     */   }
/*  472:     */   
/*  473:     */   public void writeSample(int track, byte[] data, int off, int len, long duration, boolean isSync)
/*  474:     */     throws IOException
/*  475:     */   {
/*  476: 844 */     ensureStarted();
/*  477: 845 */     if (duration <= 0L) {
/*  478: 846 */       throw new IllegalArgumentException("duration must be greater 0");
/*  479:     */     }
/*  480: 848 */     AbstractQuickTimeStream.Track t = (AbstractQuickTimeStream.Track)this.tracks.get(track);
/*  481: 849 */     ensureOpen();
/*  482: 850 */     ensureStarted();
/*  483: 851 */     long offset = getRelativeStreamPosition();
/*  484: 852 */     OutputStream mdatOut = this.mdatAtom.getOutputStream();
/*  485: 853 */     mdatOut.write(data, off, len);
/*  486: 854 */     t.addSample(new AbstractQuickTimeStream.Sample(duration, offset, len), 1, isSync);
/*  487:     */   }
/*  488:     */   
/*  489:     */   public void writeSamples(int track, int sampleCount, byte[] data, long sampleDuration)
/*  490:     */     throws IOException
/*  491:     */   {
/*  492: 875 */     writeSamples(track, sampleCount, data, 0, data.length, sampleDuration, true);
/*  493:     */   }
/*  494:     */   
/*  495:     */   public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration)
/*  496:     */     throws IOException
/*  497:     */   {
/*  498: 896 */     writeSamples(track, sampleCount, data, off, len, sampleDuration, true);
/*  499:     */   }
/*  500:     */   
/*  501:     */   public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration, boolean isSync)
/*  502:     */     throws IOException
/*  503:     */   {
/*  504: 921 */     ensureStarted();
/*  505: 922 */     if (sampleDuration <= 0L) {
/*  506: 923 */       throw new IllegalArgumentException("sampleDuration must be greater 0, sampleDuration=" + sampleDuration);
/*  507:     */     }
/*  508: 925 */     if (sampleCount <= 0) {
/*  509: 926 */       throw new IllegalArgumentException("sampleCount must be greater 0, sampleCount=" + sampleCount);
/*  510:     */     }
/*  511: 928 */     if (len % sampleCount != 0) {
/*  512: 929 */       throw new IllegalArgumentException("len must be divisable by sampleCount len=" + len + " sampleCount=" + sampleCount);
/*  513:     */     }
/*  514: 931 */     AbstractQuickTimeStream.Track t = (AbstractQuickTimeStream.Track)this.tracks.get(track);
/*  515: 932 */     ensureOpen();
/*  516: 933 */     ensureStarted();
/*  517: 934 */     long offset = getRelativeStreamPosition();
/*  518: 935 */     OutputStream mdatOut = this.mdatAtom.getOutputStream();
/*  519: 936 */     mdatOut.write(data, off, len);
/*  520:     */     
/*  521:     */ 
/*  522: 939 */     int sampleLength = len / sampleCount;
/*  523: 940 */     AbstractQuickTimeStream.Sample first = new AbstractQuickTimeStream.Sample(sampleDuration, offset, sampleLength);
/*  524: 941 */     AbstractQuickTimeStream.Sample last = new AbstractQuickTimeStream.Sample(sampleDuration, offset + sampleLength * (sampleCount - 1), sampleLength);
/*  525: 942 */     t.addChunk(new AbstractQuickTimeStream.Chunk(first, last, sampleCount, 1), isSync);
/*  526:     */   }
/*  527:     */   
/*  528:     */   public boolean isVFRSupported()
/*  529:     */   {
/*  530: 948 */     return true;
/*  531:     */   }
/*  532:     */   
/*  533:     */   public boolean isDataLimitReached()
/*  534:     */   {
/*  535:     */     try
/*  536:     */     {
/*  537: 962 */       long maxMediaDuration = 0L;
/*  538: 963 */       for (AbstractQuickTimeStream.Track t : this.tracks) {
/*  539: 964 */         maxMediaDuration = Math.max(t.mediaDuration, maxMediaDuration);
/*  540:     */       }
/*  541: 968 */       return (getRelativeStreamPosition() > 2305843009213693952L) || (maxMediaDuration > 2305843009213693952L);
/*  542:     */     }
/*  543:     */     catch (IOException ex) {}
/*  544: 970 */     return true;
/*  545:     */   }
/*  546:     */   
/*  547:     */   public void close()
/*  548:     */     throws IOException
/*  549:     */   {
/*  550:     */     try
/*  551:     */     {
/*  552: 982 */       if (this.state == AbstractQuickTimeStream.States.STARTED) {
/*  553: 983 */         finish();
/*  554:     */       }
/*  555:     */     }
/*  556:     */     finally
/*  557:     */     {
/*  558: 986 */       if (this.state != AbstractQuickTimeStream.States.CLOSED)
/*  559:     */       {
/*  560: 987 */         this.out.close();
/*  561: 988 */         this.state = AbstractQuickTimeStream.States.CLOSED;
/*  562:     */       }
/*  563:     */     }
/*  564:     */   }
/*  565:     */   
/*  566:     */   public void finish()
/*  567:     */     throws IOException
/*  568:     */   {
/*  569:1003 */     ensureOpen();
/*  570:1004 */     if (this.state != AbstractQuickTimeStream.States.FINISHED)
/*  571:     */     {
/*  572:1005 */       int i = 0;
/*  573:1005 */       for (int n = this.tracks.size(); i < n; i++) {}
/*  574:1007 */       this.mdatAtom.finish();
/*  575:1008 */       writeEpilog();
/*  576:1009 */       this.state = AbstractQuickTimeStream.States.FINISHED;
/*  577:     */     }
/*  578:     */   }
/*  579:     */   
/*  580:     */   protected void ensureOpen()
/*  581:     */     throws IOException
/*  582:     */   {
/*  583:1024 */     if (this.state == AbstractQuickTimeStream.States.CLOSED) {
/*  584:1025 */       throw new IOException("Stream closed");
/*  585:     */     }
/*  586:     */   }
/*  587:     */   
/*  588:     */   private void writeProlog()
/*  589:     */     throws IOException
/*  590:     */   {
/*  591:1041 */     AbstractQuickTimeStream.DataAtom ftypAtom = new AbstractQuickTimeStream.DataAtom("ftyp");
/*  592:1042 */     DataAtomOutputStream d = ftypAtom.getOutputStream();
/*  593:1043 */     d.writeType("qt  ");
/*  594:1044 */     d.writeBCD4(2005);
/*  595:1045 */     d.writeBCD2(3);
/*  596:1046 */     d.writeBCD2(0);
/*  597:1047 */     d.writeType("qt  ");
/*  598:1048 */     d.writeInt(0);
/*  599:1049 */     d.writeInt(0);
/*  600:1050 */     d.writeInt(0);
/*  601:1051 */     ftypAtom.finish();
/*  602:     */   }
/*  603:     */   
/*  604:     */   private void writeEpilog()
/*  605:     */     throws IOException
/*  606:     */   {
/*  607:1055 */     Date modificationTime = new Date();
/*  608:1056 */     long duration = getMovieDuration();
/*  609:     */     
/*  610:     */ 
/*  611:     */ 
/*  612:     */ 
/*  613:1061 */     this.moovAtom = new AbstractQuickTimeStream.CompositeAtom("moov");
/*  614:     */     
/*  615:     */ 
/*  616:     */ 
/*  617:     */ 
/*  618:     */ 
/*  619:     */ 
/*  620:     */ 
/*  621:     */ 
/*  622:     */ 
/*  623:     */ 
/*  624:     */ 
/*  625:     */ 
/*  626:     */ 
/*  627:     */ 
/*  628:     */ 
/*  629:     */ 
/*  630:     */ 
/*  631:     */ 
/*  632:     */ 
/*  633:     */ 
/*  634:     */ 
/*  635:     */ 
/*  636:     */ 
/*  637:     */ 
/*  638:     */ 
/*  639:     */ 
/*  640:1088 */     AbstractQuickTimeStream.DataAtom leaf = new AbstractQuickTimeStream.DataAtom("mvhd");
/*  641:1089 */     this.moovAtom.add(leaf);
/*  642:1090 */     DataAtomOutputStream d = leaf.getOutputStream();
/*  643:1091 */     d.writeByte(0);
/*  644:     */     
/*  645:     */ 
/*  646:1094 */     d.writeByte(0);
/*  647:1095 */     d.writeByte(0);
/*  648:1096 */     d.writeByte(0);
/*  649:     */     
/*  650:     */ 
/*  651:1099 */     d.writeMacTimestamp(this.creationTime);
/*  652:     */     
/*  653:     */ 
/*  654:     */ 
/*  655:     */ 
/*  656:     */ 
/*  657:1105 */     d.writeMacTimestamp(modificationTime);
/*  658:     */     
/*  659:     */ 
/*  660:     */ 
/*  661:     */ 
/*  662:     */ 
/*  663:1111 */     d.writeUInt(this.movieTimeScale);
/*  664:     */     
/*  665:     */ 
/*  666:     */ 
/*  667:     */ 
/*  668:     */ 
/*  669:1117 */     d.writeUInt(duration);
/*  670:     */     
/*  671:     */ 
/*  672:     */ 
/*  673:     */ 
/*  674:     */ 
/*  675:1123 */     d.writeFixed16D16(1.0D);
/*  676:     */     
/*  677:     */ 
/*  678:     */ 
/*  679:1127 */     d.writeShort(256);
/*  680:     */     
/*  681:     */ 
/*  682:     */ 
/*  683:1131 */     d.write(new byte[10]);
/*  684:     */     
/*  685:     */ 
/*  686:1134 */     d.writeFixed16D16(1.0D);
/*  687:1135 */     d.writeFixed16D16(0.0D);
/*  688:1136 */     d.writeFixed2D30(0.0D);
/*  689:1137 */     d.writeFixed16D16(0.0D);
/*  690:1138 */     d.writeFixed16D16(1.0D);
/*  691:1139 */     d.writeFixed2D30(0.0D);
/*  692:1140 */     d.writeFixed16D16(0.0D);
/*  693:1141 */     d.writeFixed16D16(0.0D);
/*  694:1142 */     d.writeFixed2D30(1.0D);
/*  695:     */     
/*  696:     */ 
/*  697:     */ 
/*  698:     */ 
/*  699:     */ 
/*  700:1148 */     d.writeInt(0);
/*  701:     */     
/*  702:     */ 
/*  703:1151 */     d.writeInt(0);
/*  704:     */     
/*  705:     */ 
/*  706:1154 */     d.writeInt(0);
/*  707:     */     
/*  708:     */ 
/*  709:1157 */     d.writeInt(0);
/*  710:     */     
/*  711:     */ 
/*  712:1160 */     d.writeInt(0);
/*  713:     */     
/*  714:     */ 
/*  715:1163 */     d.writeInt(0);
/*  716:     */     
/*  717:     */ 
/*  718:1166 */     d.writeUInt(this.tracks.size() + 1);
/*  719:     */     
/*  720:     */ 
/*  721:     */ 
/*  722:     */ 
/*  723:1171 */     int i = 0;
/*  724:1171 */     for (int n = this.tracks.size(); i < n; i++)
/*  725:     */     {
/*  726:1172 */       AbstractQuickTimeStream.Track t = (AbstractQuickTimeStream.Track)this.tracks.get(i);
/*  727:     */       
/*  728:1174 */       t.writeTrackAtoms(i, this.moovAtom, modificationTime);
/*  729:     */     }
/*  730:1177 */     this.moovAtom.finish();
/*  731:     */   }
/*  732:     */   
/*  733:     */   public void toWebOptimizedMovie(File outputFile, boolean compressHeader)
/*  734:     */     throws IOException
/*  735:     */   {
/*  736:1190 */     finish();
/*  737:1191 */     long originalMdatOffset = this.mdatAtom.getOffset();
/*  738:1192 */     AbstractQuickTimeStream.CompositeAtom originalMoovAtom = this.moovAtom;
/*  739:1193 */     this.mdatOffset = 0L;
/*  740:     */     
/*  741:1195 */     ImageOutputStream originalOut = this.out;
/*  742:     */     try
/*  743:     */     {
/*  744:1197 */       this.out = null;
/*  745:1199 */       if (compressHeader)
/*  746:     */       {
/*  747:1200 */         ByteArrayOutputStream buf = new ByteArrayOutputStream();
/*  748:1201 */         int maxIteration = 5;
/*  749:1202 */         long compressionHeadersSize = 48L;
/*  750:1203 */         long headerSize = 0L;
/*  751:1204 */         long freeSize = 0L;
/*  752:     */         for (;;)
/*  753:     */         {
/*  754:1206 */           this.mdatOffset = (compressionHeadersSize + headerSize + freeSize);
/*  755:1207 */           buf.reset();
/*  756:1208 */           DeflaterOutputStream deflater = new DeflaterOutputStream(buf);
/*  757:1209 */           this.out = new MemoryCacheImageOutputStream(deflater);
/*  758:1210 */           writeEpilog();
/*  759:1211 */           this.out.close();
/*  760:1212 */           deflater.close();
/*  761:1214 */           if (buf.size() <= headerSize + freeSize) {
/*  762:     */             break;
/*  763:     */           }
/*  764:1214 */           maxIteration--;
/*  765:1214 */           if (maxIteration <= 0) {
/*  766:     */             break;
/*  767:     */           }
/*  768:1215 */           if (headerSize != 0L) {
/*  769:1216 */             freeSize = Math.max(freeSize, buf.size() - headerSize - freeSize);
/*  770:     */           }
/*  771:1218 */           headerSize = buf.size();
/*  772:     */         }
/*  773:1220 */         freeSize = headerSize + freeSize - buf.size();
/*  774:1221 */         headerSize = buf.size();
/*  775:1226 */         if ((maxIteration < 0) || (buf.size() == 0))
/*  776:     */         {
/*  777:1227 */           compressHeader = false;
/*  778:1228 */           System.err.println("WARNING QuickTimeWriter failed to compress header.");
/*  779:     */         }
/*  780:     */         else
/*  781:     */         {
/*  782:1230 */           this.out = new FileImageOutputStream(outputFile);
/*  783:1231 */           writeProlog();
/*  784:     */           
/*  785:     */ 
/*  786:     */ 
/*  787:1235 */           DataAtomOutputStream daos = new DataAtomOutputStream(new ImageOutputStreamAdapter(this.out));
/*  788:1236 */           daos.writeUInt(headerSize + 40L);
/*  789:1237 */           daos.writeType("moov");
/*  790:     */           
/*  791:1239 */           daos.writeUInt(headerSize + 32L);
/*  792:1240 */           daos.writeType("cmov");
/*  793:     */           
/*  794:1242 */           daos.writeUInt(12L);
/*  795:1243 */           daos.writeType("dcom");
/*  796:1244 */           daos.writeType("zlib");
/*  797:     */           
/*  798:1246 */           daos.writeUInt(headerSize + 12L);
/*  799:1247 */           daos.writeType("cmvd");
/*  800:1248 */           daos.writeUInt(originalMoovAtom.size());
/*  801:     */           
/*  802:1250 */           daos.write(buf.toByteArray());
/*  803:     */           
/*  804:     */ 
/*  805:1253 */           daos.writeUInt(freeSize + 8L);
/*  806:1254 */           daos.writeType("free");
/*  807:1255 */           for (int i = 0; i < freeSize; i++) {
/*  808:1256 */             daos.write(0);
/*  809:     */           }
/*  810:     */         }
/*  811:     */       }
/*  812:1261 */       if (!compressHeader)
/*  813:     */       {
/*  814:1262 */         this.out = new FileImageOutputStream(outputFile);
/*  815:1263 */         this.mdatOffset = this.moovAtom.size();
/*  816:1264 */         writeProlog();
/*  817:1265 */         writeEpilog();
/*  818:     */       }
/*  819:1269 */       byte[] buf = new byte[4096];
/*  820:1270 */       originalOut.seek(originalMdatOffset);
/*  821:1271 */       long count = 0L;
/*  822:1271 */       for (long n = this.mdatAtom.size(); count < n;)
/*  823:     */       {
/*  824:1272 */         int read = originalOut.read(buf, 0, (int)Math.min(buf.length, n - count));
/*  825:1273 */         this.out.write(buf, 0, read);
/*  826:1274 */         count += read;
/*  827:     */       }
/*  828:1276 */       this.out.close();
/*  829:     */     }
/*  830:     */     finally
/*  831:     */     {
/*  832:1278 */       this.mdatOffset = 0L;
/*  833:1279 */       this.moovAtom = originalMoovAtom;
/*  834:1280 */       this.out = originalOut;
/*  835:     */     }
/*  836:     */   }
/*  837:     */ }

