/*    1:     */ package asj.testrecorder.media.avi;
/*    2:     */ 
/*    3:     */ import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Codec;
import asj.testrecorder.media.MovieWriter;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.jpeg.JPEGCodec;
import asj.testrecorder.media.png.PNGCodec;

/*    9:     */ import java.awt.Dimension;
/*   10:     */ import java.awt.Rectangle;
/*   11:     */ import java.awt.image.BufferedImage;
/*   12:     */ import java.awt.image.IndexColorModel;
/*   13:     */ import java.io.File;
/*   14:     */ import java.io.FileInputStream;
/*   15:     */ import java.io.IOException;
/*   16:     */ import java.io.InputStream;
/*   17:     */ import java.io.OutputStream;
/*   18:     */ import java.util.ArrayList;
/*   19:     */ import java.util.Arrays;
/*   20:     */ import java.util.Iterator;
/*   21:     */ import java.util.LinkedList;
/*   22:     */ import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
/*   24:     */ 
/*   25:     */ public class AVIWriter
/*   26:     */   extends AbstractAVIStream
/*   27:     */   implements MovieWriter
/*   28:     */ {
/*   29:  62 */   public static final VideoFormat VIDEO_RAW = new VideoFormat("DIB ");
/*   30:  63 */   public static final VideoFormat VIDEO_JPEG = new VideoFormat("MJPG");
/*   31:  64 */   public static final VideoFormat VIDEO_PNG = new VideoFormat("png ");
/*   32:  65 */   public static final VideoFormat VIDEO_SCREEN_CAPTURE = new VideoFormat("tscc");
/*   33:     */   
/*   34:     */   private static enum States
/*   35:     */   {
/*   36:  72 */     STARTED,  FINISHED,  CLOSED;
/*   37:     */   }
/*   38:     */   
/*   39:  77 */   private States state = States.FINISHED;
/*   40:     */   private AbstractAVIStream.CompositeChunk aviChunk;
/*   41:     */   private AbstractAVIStream.CompositeChunk moviChunk;
/*   42:     */   AbstractAVIStream.FixedSizeDataChunk avihChunk;
/*   43:     */   
/*   44:     */   public AVIWriter(File file)
/*   45:     */     throws IOException
/*   46:     */   {
/*   47:  97 */     if (file.exists()) {
/*   48:  98 */       file.delete();
/*   49:     */     }
/*   50: 100 */     this.out = new FileImageOutputStream(file);
/*   51: 101 */     this.streamOffset = 0L;
/*   52:     */   }
/*   53:     */   
/*   54:     */   public AVIWriter(ImageOutputStream out)
/*   55:     */     throws IOException
/*   56:     */   {
/*   57: 110 */     this.out = out;
/*   58: 111 */     this.streamOffset = out.getStreamPosition();
/*   59:     */   }
/*   60:     */   
/*   61:     */   public int addVideoTrack(VideoFormat format, long timeScale, long frameRate, int width, int height, int depth, int syncInterval)
/*   62:     */     throws IOException
/*   63:     */   {
/*   64: 135 */     return addVideoTrack(format.getEncoding(), timeScale, frameRate, width, height, depth, syncInterval);
/*   65:     */   }
/*   66:     */   
/*   67:     */   public int addVideoTrack(VideoFormat format, long timeScale, long frameRate, int syncInterval)
/*   68:     */     throws IOException
/*   69:     */   {
/*   70: 153 */     return addVideoTrack(format.getEncoding(), timeScale, frameRate, format.getWidth(), format.getHeight(), format.getDepth(), syncInterval);
/*   71:     */   }
/*   72:     */   
/*   73:     */   public int addVideoTrack(VideoFormat format, long timeScale, long frameRate, int width, int height)
/*   74:     */     throws IOException
/*   75:     */   {
/*   76: 171 */     return addVideoTrack(format.getEncoding(), timeScale, frameRate, width, height, 24, 24);
/*   77:     */   }
/*   78:     */   
/*   79:     */   public int addVideoTrack(String fourCC, long timeScale, long frameRate, int width, int height, int depth, int syncInterval)
/*   80:     */     throws IOException
/*   81:     */   {
/*   82: 192 */     AbstractAVIStream.VideoTrack vt = new AbstractAVIStream.VideoTrack(this.tracks.size(), fourCC);
/*   83: 193 */     vt.videoFormat = new VideoFormat(fourCC, Byte[].class, width, height, depth);
/*   84: 194 */     vt.timeScale = timeScale;
/*   85: 195 */     vt.frameRate = frameRate;
/*   86: 196 */     vt.syncInterval = syncInterval;
/*   87: 197 */     vt.rcFrame = new Rectangle(0, 0, width, height);
/*   88:     */     
/*   89: 199 */     vt.samples = new LinkedList();
/*   90: 201 */     if (vt.videoFormat.getDepth() == 4)
/*   91:     */     {
/*   92: 202 */       byte[] gray = new byte[16];
/*   93: 203 */       for (int i = 0; i < gray.length; i++) {
/*   94: 204 */         gray[i] = ((byte)(i << 4 | i));
/*   95:     */       }
/*   96: 206 */       vt.palette = new IndexColorModel(4, 16, gray, gray, gray);
/*   97:     */     }
/*   98: 207 */     else if (vt.videoFormat.getDepth() == 8)
/*   99:     */     {
/*  100: 208 */       byte[] gray = new byte[256];
/*  101: 209 */       for (int i = 0; i < gray.length; i++) {
/*  102: 210 */         gray[i] = ((byte)i);
/*  103:     */       }
/*  104: 212 */       vt.palette = new IndexColorModel(8, 256, gray, gray, gray);
/*  105:     */     }
/*  106: 214 */     createCodec(vt);
/*  107:     */     
/*  108: 216 */     this.tracks.add(vt);
/*  109: 217 */     return this.tracks.size() - 1;
/*  110:     */   }
/*  111:     */   
/*  112:     */   public void setPalette(int track, IndexColorModel palette)
/*  113:     */   {
/*  114: 222 */     ((AbstractAVIStream.VideoTrack)this.tracks.get(track)).palette = palette;
/*  115:     */   }
/*  116:     */   
/*  117:     */   public void setCompressionQuality(int track, float newValue)
/*  118:     */   {
/*  119: 241 */     AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)this.tracks.get(track);
/*  120: 242 */     vt.videoQuality = newValue;
/*  121: 243 */     if (vt.codec != null) {
/*  122: 244 */       vt.codec.setQuality(newValue);
/*  123:     */     }
/*  124:     */   }
/*  125:     */   
/*  126:     */   public float getVideoCompressionQuality(int track)
/*  127:     */   {
/*  128: 254 */     return ((AbstractAVIStream.VideoTrack)this.tracks.get(track)).videoQuality;
/*  129:     */   }
/*  130:     */   
/*  131:     */   public void setVideoDimension(int track, int width, int height)
/*  132:     */   {
/*  133: 270 */     if ((width < 1) || (height < 1)) {
/*  134: 271 */       throw new IllegalArgumentException("width and height must be greater zero.");
/*  135:     */     }
/*  136: 273 */     AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)this.tracks.get(track);
/*  137: 274 */     vt.videoFormat = new VideoFormat(vt.videoFormat.getEncoding(), Byte[].class, width, height, vt.videoFormat.getDepth());
/*  138:     */   }
/*  139:     */   
/*  140:     */   public Dimension getVideoDimension(int track)
/*  141:     */   {
/*  142: 283 */     AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)this.tracks.get(track);
/*  143: 284 */     VideoFormat fmt = vt.videoFormat;
/*  144: 285 */     return new Dimension(fmt.getWidth(), fmt.getHeight());
/*  145:     */   }
/*  146:     */   
/*  147:     */   private void ensureStarted()
/*  148:     */     throws IOException
/*  149:     */   {
/*  150: 295 */     if (this.state != States.STARTED)
/*  151:     */     {
/*  152: 296 */       writeProlog();
/*  153: 297 */       this.state = States.STARTED;
/*  154:     */     }
/*  155:     */   }
/*  156:     */   
/*  157:     */   public void writeFrame(int track, BufferedImage image, long duration)
/*  158:     */     throws IOException
/*  159:     */   {
/*  160: 320 */     ensureStarted();
/*  161:     */     
/*  162: 322 */     AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)this.tracks.get(track);
/*  163: 323 */     if (vt.codec == null) {
/*  164: 324 */       throw new UnsupportedOperationException("No codec for this video format.");
/*  165:     */     }
/*  166: 328 */     VideoFormat fmt = vt.videoFormat;
/*  167: 329 */     if ((fmt.getWidth() != image.getWidth()) || (fmt.getHeight() != image.getHeight())) {
/*  168: 330 */       throw new IllegalArgumentException("Dimensions of image[" + vt.samples.size() + 
/*  169: 331 */         "] (width=" + image.getWidth() + ", height=" + image.getHeight() + 
/*  170: 332 */         ") differs from image[0] (width=" + 
/*  171: 333 */         fmt.getWidth() + ", height=" + fmt.getHeight());
/*  172:     */     }
/*  173: 339 */     long offset = getRelativeStreamPosition();
/*  174: 340 */     switch (fmt.getDepth())
/*  175:     */     {
/*  176:     */     case 4: 
/*  177: 342 */       IndexColorModel imgPalette = (IndexColorModel)image.getColorModel();
/*  178: 343 */       int[] imgRGBs = new int[16];
/*  179: 344 */       imgPalette.getRGBs(imgRGBs);
/*  180: 345 */       int[] previousRGBs = new int[16];
/*  181: 346 */       if (vt.previousPalette == null) {
/*  182: 347 */         vt.previousPalette = vt.palette;
/*  183:     */       }
/*  184: 349 */       vt.previousPalette.getRGBs(previousRGBs);
/*  185: 350 */       if (!Arrays.equals(imgRGBs, previousRGBs))
/*  186:     */       {
/*  187: 351 */         vt.previousPalette = imgPalette;
/*  188: 352 */         AbstractAVIStream.DataChunk paletteChangeChunk = new AbstractAVIStream.DataChunk(vt.twoCC + "pc");
/*  189:     */         
/*  190:     */ 
/*  191:     */ 
/*  192:     */ 
/*  193:     */ 
/*  194:     */ 
/*  195:     */ 
/*  196:     */ 
/*  197:     */ 
/*  198:     */ 
/*  199:     */ 
/*  200: 364 */         int first = 0;
/*  201: 365 */         int last = imgPalette.getMapSize() - 1;
/*  202:     */         
/*  203:     */ 
/*  204:     */ 
/*  205:     */ 
/*  206:     */ 
/*  207:     */ 
/*  208:     */ 
/*  209:     */ 
/*  210:     */ 
/*  211:     */ 
/*  212:     */ 
/*  213:     */ 
/*  214:     */ 
/*  215:     */ 
/*  216:     */ 
/*  217: 381 */         DataChunkOutputStream pOut = paletteChangeChunk.getOutputStream();
/*  218: 382 */         pOut.writeByte(first);
/*  219: 383 */         pOut.writeByte(last - first + 1);
/*  220: 384 */         pOut.writeShort(0);
/*  221: 386 */         for (int i = first; i <= last; i++)
/*  222:     */         {
/*  223: 387 */           pOut.writeByte(imgRGBs[i] >>> 16 & 0xFF);
/*  224: 388 */           pOut.writeByte(imgRGBs[i] >>> 8 & 0xFF);
/*  225: 389 */           pOut.writeByte(imgRGBs[i] & 0xFF);
/*  226: 390 */           pOut.writeByte(0);
/*  227:     */         }
/*  228: 393 */         this.moviChunk.add(paletteChangeChunk);
/*  229: 394 */         paletteChangeChunk.finish();
/*  230: 395 */         long length = getRelativeStreamPosition() - offset;
/*  231: 396 */         vt.samples.add(new AbstractAVIStream.Sample(paletteChangeChunk.chunkType, 0, offset, length - 8L, false));
/*  232: 397 */         offset = getRelativeStreamPosition();
/*  233:     */       }
/*  234: 399 */       break;
/*  235:     */     case 8: 
/*  236: 402 */       imgPalette = (IndexColorModel)image.getColorModel();
/*  237: 403 */       imgRGBs = new int[256];
/*  238: 404 */       imgPalette.getRGBs(imgRGBs);
/*  239: 405 */       previousRGBs = new int[256];
/*  240: 406 */       if (vt.previousPalette == null) {
/*  241: 407 */         vt.previousPalette = vt.palette;
/*  242:     */       }
/*  243: 409 */       vt.previousPalette.getRGBs(previousRGBs);
/*  244: 410 */       if (!Arrays.equals(imgRGBs, previousRGBs))
/*  245:     */       {
/*  246: 411 */         vt.previousPalette = imgPalette;
/*  247: 412 */         AbstractAVIStream.DataChunk paletteChangeChunk = new AbstractAVIStream.DataChunk( vt.twoCC + "pc");
/*  248:     */         
/*  249:     */ 
/*  250:     */ 
/*  251:     */ 
/*  252:     */ 
/*  253:     */ 
/*  254:     */ 
/*  255:     */ 
/*  256:     */ 
/*  257:     */ 
/*  258:     */ 
/*  259: 424 */         int first = 0;
/*  260: 425 */         int last = imgPalette.getMapSize() - 1;
/*  261:     */         
/*  262:     */ 
/*  263:     */ 
/*  264:     */ 
/*  265:     */ 
/*  266:     */ 
/*  267:     */ 
/*  268:     */ 
/*  269:     */ 
/*  270:     */ 
/*  271:     */ 
/*  272:     */ 
/*  273:     */ 
/*  274:     */ 
/*  275:     */ 
/*  276: 441 */         DataChunkOutputStream pOut = paletteChangeChunk.getOutputStream();
/*  277: 442 */         pOut.writeByte(first);
/*  278: 443 */         pOut.writeByte(last - first + 1);
/*  279: 444 */         pOut.writeShort(0);
/*  280: 446 */         for (int i = first; i <= last; i++)
/*  281:     */         {
/*  282: 447 */           pOut.writeByte(imgRGBs[i] >>> 16 & 0xFF);
/*  283: 448 */           pOut.writeByte(imgRGBs[i] >>> 8 & 0xFF);
/*  284: 449 */           pOut.writeByte(imgRGBs[i] & 0xFF);
/*  285: 450 */           pOut.writeByte(0);
/*  286:     */         }
/*  287: 453 */         this.moviChunk.add(paletteChangeChunk);
/*  288: 454 */         paletteChangeChunk.finish();
/*  289: 455 */         long length = getRelativeStreamPosition() - offset;
/*  290: 456 */         vt.samples.add(new AbstractAVIStream.Sample(paletteChangeChunk.chunkType, 0, offset, length - 8L, false));
/*  291: 457 */         offset = getRelativeStreamPosition();
/*  292:     */       }
/*  293:     */       break;
/*  294:     */     }
/*  295: 466 */     if (vt.outputBuffer == null) {
/*  296: 467 */       vt.outputBuffer = new Buffer();
/*  297:     */     }
/*  298: 470 */     boolean isSync = vt.syncInterval != 0;
/*  299:     */     
/*  300: 472 */     Buffer inputBuffer = new Buffer();
/*  301: 473 */     inputBuffer.flags = (isSync ? 16 : 0);
/*  302: 474 */     inputBuffer.data = image;
/*  303: 475 */     vt.codec.process(inputBuffer, vt.outputBuffer);
/*  304: 476 */     if (vt.outputBuffer.flags == 2) {
/*  305: 477 */       return;
/*  306:     */     }
/*  307: 480 */     isSync = (vt.outputBuffer.flags & 0x10) != 0;
/*  308:     */     
/*  309: 482 */      offset = getRelativeStreamPosition();
/*  310:     */     
/*  311: 484 */     AbstractAVIStream.DataChunk videoFrameChunk = new AbstractAVIStream.DataChunk(vt.twoCC + "dc");
/*  313: 486 */     this.moviChunk.add(videoFrameChunk);
/*  314: 487 */     videoFrameChunk.getOutputStream().write((byte[])vt.outputBuffer.data, vt.outputBuffer.offset, vt.outputBuffer.length);
/*  315: 488 */     videoFrameChunk.finish();
/*  316: 489 */     long length = getRelativeStreamPosition() - offset;
/*  317:     */     
/*  318: 491 */     vt.samples.add(new AbstractAVIStream.Sample(videoFrameChunk.chunkType, (int)vt.frameRate, offset, length - 8L, isSync));
/*  319: 492 */     if (getRelativeStreamPosition() > 4294967296L) {
/*  320: 493 */       throw new IOException("AVI file is larger than 4 GB");
/*  321:     */     }
/*  322:     */   }
/*  323:     */   
/*  324:     */   private void createCodec(AbstractAVIStream.VideoTrack vt)
/*  325:     */   {
/*  326: 499 */     VideoFormat fmt = vt.videoFormat;
/*  327: 500 */     String enc = fmt.getEncoding();
/*  328: 501 */     if (enc.equals("MJPG")) {
/*  329: 502 */       vt.codec = new JPEGCodec();
/*  330: 503 */     } else if (enc.equals("png ")) {
/*  331: 504 */       vt.codec = new PNGCodec();
/*  332: 505 */     } else if (enc.equals("DIB ")) {
/*  333: 506 */       vt.codec = new DIBCodec();
/*  334: 507 */     } else if (enc.equals("RLE ")) {
/*  335: 508 */       vt.codec = new RunLengthCodec();
/*  336: 509 */     } else if (enc.equals("tscc")) {
/*  337: 510 */       vt.codec = new TechSmithCodec();
/*  338:     */     }
/*  339: 513 */     vt.codec.setInputFormat(new VideoFormat(enc, BufferedImage.class, fmt.getWidth(), fmt.getHeight(), fmt.getDepth()));
/*  340: 514 */     vt.codec.setOutputFormat(new VideoFormat(enc, Byte[].class, fmt.getWidth(), fmt.getHeight(), fmt.getDepth()));
/*  341: 515 */     vt.codec.setQuality(vt.videoQuality);
/*  342:     */   }
/*  343:     */   
/*  344:     */   public void writeFrame(int track, File file)
/*  345:     */     throws IOException
/*  346:     */   {
/*  347: 535 */     FileInputStream in = null;
/*  348:     */     try
/*  349:     */     {
/*  350: 537 */       in = new FileInputStream(file);
/*  351: 538 */       writeFrame(track, in);
/*  352:     */     }
/*  353:     */     finally
/*  354:     */     {
/*  355: 540 */       if (in != null) {
/*  356: 541 */         in.close();
/*  357:     */       }
/*  358:     */     }
/*  359:     */   }
/*  360:     */   
/*  361:     */   public void writeFrame(int track, InputStream in)
/*  362:     */     throws IOException
/*  363:     */   {
/*  364: 563 */     ensureStarted();
/*  365:     */     
/*  366: 565 */     AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)this.tracks.get(track);
/*  367:     */     
/*  368: 567 */     AbstractAVIStream.DataChunk videoFrameChunk = new AbstractAVIStream.DataChunk(vt.twoCC + "dc");
/*  370: 569 */     this.moviChunk.add(videoFrameChunk);
/*  371: 570 */     OutputStream mdatOut = videoFrameChunk.getOutputStream();
/*  372: 571 */     long offset = getRelativeStreamPosition();
/*  373: 572 */     byte[] buf = new byte[512];
/*  374:     */     int len;
/*  375: 574 */     while ((len = in.read(buf)) != -1)
/*  376:     */     {

/*  378: 575 */       mdatOut.write(buf, 0, len);
/*  379:     */     }
/*  380: 577 */     long length = getRelativeStreamPosition() - offset;
/*  381: 578 */     videoFrameChunk.finish();
/*  382: 579 */     vt.samples.add(new AbstractAVIStream.Sample(videoFrameChunk.chunkType, (int)vt.frameRate, offset, length - 8L, true));
/*  383: 580 */     if (getRelativeStreamPosition() > 4294967296L) {
/*  384: 581 */       throw new IOException("AVI file is larger than 4 GB");
/*  385:     */     }
/*  386:     */   }
/*  387:     */   
/*  388:     */   public void writeSample(int track, byte[] data, int off, int len, long duration, boolean isSync)
/*  389:     */     throws IOException
/*  390:     */   {
/*  391: 603 */     ensureStarted();
/*  392: 604 */     AbstractAVIStream.Track t = (AbstractAVIStream.Track)this.tracks.get(track);
/*  393:     */     AbstractAVIStream.DataChunk dc;
/*  394: 606 */     if ((t instanceof AbstractAVIStream.VideoTrack))
/*  395:     */     {
/*  396: 607 */       AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)t;
/*  397: 608 */       dc = new AbstractAVIStream.DataChunk(vt.twoCC + "dc");
/*  399:     */     }
/*  400:     */     else
/*  401:     */     {
/*  402: 611 */       throw new UnsupportedOperationException("Not yet implemented");
/*  403:     */     }
/*  405: 613 */     this.moviChunk.add(dc);
/*  406: 614 */     OutputStream mdatOut = dc.getOutputStream();
/*  407: 615 */     long offset = getRelativeStreamPosition();
/*  408: 616 */     mdatOut.write(data, off, len);
/*  409: 617 */     long length = getRelativeStreamPosition() - offset;
/*  410: 618 */     dc.finish();
/*  411: 619 */     t.samples.add(new AbstractAVIStream.Sample(dc.chunkType, (int)t.frameRate, offset, length - 8L, true));
/*  412: 620 */     if (getRelativeStreamPosition() > 4294967296L) {
/*  413: 621 */       throw new IOException("AVI file is larger than 4 GB");
/*  414:     */     }
/*  415:     */   }
/*  416:     */   
/*  417:     */   public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration, boolean isSync)
/*  418:     */     throws IOException
/*  419:     */   {
/*  420: 647 */     for (int i = 0; i < sampleCount; i++)
/*  421:     */     {
/*  422: 648 */       writeSample(track, data, off, len / sampleCount, sampleDuration, isSync);
/*  423: 649 */       off += len / sampleCount;
/*  424:     */     }
/*  425:     */   }
/*  426:     */   
/*  427:     */   public void close()
/*  428:     */     throws IOException
/*  429:     */   {
/*  430: 660 */     if (this.state == States.STARTED) {
/*  431: 661 */       finish();
/*  432:     */     }
/*  433: 663 */     if (this.state != States.CLOSED)
/*  434:     */     {
/*  435: 664 */       this.out.close();
/*  436: 665 */       this.state = States.CLOSED;
/*  437:     */     }
/*  438:     */   }
/*  439:     */   
/*  440:     */   public void finish()
/*  441:     */     throws IOException
/*  442:     */   {
/*  443: 679 */     ensureOpen();
/*  444: 680 */     if (this.state != States.FINISHED)
/*  445:     */     {
/*  446: 681 */       for (AbstractAVIStream.Track tr : this.tracks) {
/*  447: 682 */         if ((tr instanceof AbstractAVIStream.VideoTrack))
/*  448:     */         {
/*  449: 683 */           AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)tr;
/*  450: 684 */           VideoFormat fmt = vt.videoFormat;
/*  451: 685 */           if ((fmt.getWidth() == -1) || (fmt.getHeight() == -1)) {
/*  452: 686 */             throw new IllegalStateException("image width and height must be specified");
/*  453:     */           }
/*  454:     */         }
/*  455:     */       }
/*  456: 690 */       this.moviChunk.finish();
/*  457: 691 */       writeEpilog();
/*  458: 692 */       this.state = States.FINISHED;
/*  459:     */     }
/*  460:     */   }
/*  461:     */   
/*  462:     */   private void ensureOpen()
/*  463:     */     throws IOException
/*  464:     */   {
/*  465: 700 */     if (this.state == States.CLOSED) {
/*  466: 701 */       throw new IOException("Stream closed");
/*  467:     */     }
/*  468:     */   }
/*  469:     */   
/*  470:     */   public boolean isVFRSupported()
/*  471:     */   {
/*  472: 708 */     return false;
/*  473:     */   }
/*  474:     */   
/*  475:     */   public boolean isDataLimitReached()
/*  476:     */   {
/*  477:     */     try
/*  478:     */     {
/*  479: 720 */       return getRelativeStreamPosition() > 1932735283L;
/*  480:     */     }
/*  481:     */     catch (IOException ex) {}
/*  482: 722 */     return true;
/*  483:     */   }
/*  484:     */   
/*  485:     */   private void writeProlog()
/*  486:     */     throws IOException
/*  487:     */   {
/*  488: 739 */     this.aviChunk = new AbstractAVIStream.CompositeChunk( "RIFF", "AVI ");
/*  489: 740 */     AbstractAVIStream.CompositeChunk hdrlChunk = new AbstractAVIStream.CompositeChunk ("LIST", "hdrl");
/*  490:     */     
/*  491:     */ 
/*  492: 743 */     this.aviChunk.add(hdrlChunk);
/*  493: 744 */     this.avihChunk = new AbstractAVIStream.FixedSizeDataChunk("avih", 56L);
/*  494: 745 */     this.avihChunk.seekToEndOfChunk();
/*  495: 746 */     hdrlChunk.add(this.avihChunk);
/*  496:     */     
/*  497: 748 */     AbstractAVIStream.CompositeChunk strlChunk = new AbstractAVIStream.CompositeChunk("LIST", "strl");
/*  498: 749 */     hdrlChunk.add(strlChunk);
/*  499: 752 */     for (AbstractAVIStream.Track tr : this.tracks) {
/*  500: 753 */       if ((tr instanceof AbstractAVIStream.VideoTrack))
/*  501:     */       {
/*  502: 754 */         AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)tr;
/*  503: 755 */         vt.strhChunk = new AbstractAVIStream.FixedSizeDataChunk("strh", 56L);
/*  504: 756 */         vt.strhChunk.seekToEndOfChunk();
/*  505: 757 */         strlChunk.add(vt.strhChunk);
/*  506: 758 */         vt.strfChunk = new AbstractAVIStream.FixedSizeDataChunk("strf", vt.palette == null ? 40 : 40 + vt.palette.getMapSize() * 4);
/*  507: 759 */         vt.strfChunk.seekToEndOfChunk();
/*  508: 760 */         strlChunk.add(vt.strfChunk);
/*  509:     */       }
/*  510:     */       else
/*  511:     */       {
/*  512: 762 */         throw new UnsupportedOperationException("Track type not implemented yet.");
/*  513:     */       }
/*  514:     */     }
/*  515: 766 */     this.moviChunk = new AbstractAVIStream.CompositeChunk("LIST", "movi");
/*  516: 767 */     this.aviChunk.add(this.moviChunk);
/*  517:     */   }
/*  518:     */   
private void writeEpilog() throws IOException {

    long largestBufferSize = 0L;

    long duration = 0L;

    for (final AbstractAVIStream.Track tr : this.tracks) {

        if (tr instanceof AbstractAVIStream.VideoTrack) {

            final AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)tr;

            long trackDuration = 0L;

            for (final AbstractAVIStream.Sample s : vt.samples) {

                trackDuration += s.duration;

            }

            duration = Math.max(duration, trackDuration);

            for (final AbstractAVIStream.Sample s : vt.samples) {

                if (s.length > largestBufferSize) {

                    largestBufferSize = s.length;

                }

            }

        }

    }
/*  545: 811 */     AbstractAVIStream.DataChunk idx1Chunk = new AbstractAVIStream.DataChunk("idx1");
/*  546: 812 */     this.aviChunk.add(idx1Chunk);
/*  547: 813 */     DataChunkOutputStream d = idx1Chunk.getOutputStream();
/*  548: 814 */     long moviListOffset = this.moviChunk.offset + 8L;
/*  549: 816 */     for (AbstractAVIStream.Track tr : this.tracks) {
/*  550: 817 */       if ((tr instanceof AbstractAVIStream.VideoTrack))
/*  551:     */       {
/*  552: 819 */         AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack)tr;
/*  553: 820 */         for (AbstractAVIStream.Sample f : vt.samples)
/*  554:     */         {
/*  555: 822 */           d.writeType(f.chunkType);
/*  556:     */           
/*  557:     */ 
/*  558:     */ 
/*  559:     */ 
/*  560:     */ 
/*  561:     */ 
/*  562:     */ 
/*  563:     */ 
/*  564:     */ 
/*  565:     */ 
/*  566: 833 */           d.writeUInt((f.chunkType.endsWith("pc") ? 256 : 0) | 
/*  567: 834 */             (f.isSync ? 16 : 0));
/*  568:     */           
/*  569:     */ 
/*  570:     */ 
/*  571:     */ 
/*  572:     */ 
/*  573:     */ 
/*  574:     */ 
/*  575:     */ 
/*  576:     */ 
/*  577:     */ 
/*  578: 845 */           d.writeUInt(f.offset - moviListOffset);
/*  579:     */           
/*  580:     */ 
/*  581:     */ 
/*  582:     */ 
/*  583:     */ 
/*  584: 851 */           d.writeUInt(f.length);
/*  585:     */         }
/*  586:     */       }
/*  587:     */       else
/*  588:     */       {
/*  589: 855 */         throw new UnsupportedOperationException("Track type not yet implemented.");
/*  590:     */       }
/*  591:     */     }
/*  592: 858 */     idx1Chunk.finish();
/*  593:     */     
/*  594:     */ 
/*  595:     */ 
/*  596:     */ 
/*  597:     */ 
/*  598:     */ 
/*  599:     */ 
/*  600:     */ 
/*  601:     */ 
/*  602:     */ 
/*  603:     */ 
/*  604:     */ 
/*  605:     */ 
/*  606:     */ 
/*  607:     */ 
/*  608:     */ 
/*  609:     */ 
/*  610:     */ 
/*  611:     */ 
/*  612:     */ 
/*  613: 879 */     this.avihChunk.seekToStartOfData();
/*  614: 880 */     d = this.avihChunk.getOutputStream();
/*  615:     */     
/*  616:     */ 
/*  617: 883 */     AbstractAVIStream.Track tt = (AbstractAVIStream.Track)this.tracks.get(0);
/*  618:     */     
/*  619: 885 */     d.writeUInt(1000000L * tt.timeScale / tt.frameRate);
/*  620:     */     
/*  621:     */ 
/*  622:     */ 
/*  623: 889 */     d.writeUInt(0L);
/*  624:     */     
/*  625:     */ 
/*  626:     */ 
/*  627:     */ 
/*  628:     */ 
/*  629: 895 */     d.writeUInt(0L);
/*  630:     */     
/*  631:     */ 
/*  632:     */ 
/*  633: 899 */     d.writeUInt(48L);
/*  634:     */     
/*  635:     */ 
/*  636:     */ 
/*  637:     */ 
/*  638:     */ 
/*  639:     */ 
/*  640:     */ 
/*  641:     */ 
/*  642:     */ 
/*  643:     */ 
/*  644:     */ 
/*  645:     */ 
/*  646:     */ 
/*  647:     */ 
/*  648:     */ 
/*  649:     */ 
/*  650:     */ 
/*  651:     */ 
/*  652:     */ 
/*  653:     */ 
/*  654:     */ 
/*  655:     */ 
/*  656: 922 */     long dwTotalFrames = 0L;
/*  657: 923 */     for (AbstractAVIStream.Track t : this.tracks) {
/*  658: 924 */       dwTotalFrames += t.samples.size();
/*  659:     */     }
/*  660: 926 */     d.writeUInt(dwTotalFrames);
/*  661:     */     
/*  662:     */ 
/*  663: 929 */     d.writeUInt(0L);
/*  664:     */     
/*  665:     */ 
/*  666:     */ 
/*  667:     */ 
/*  668:     */ 
/*  669:     */ 
/*  670:     */ 
/*  671:     */ 
/*  672:     */ 
/*  673:     */ 
/*  674:     */ 
/*  675:     */ 
/*  676: 942 */     d.writeUInt(1L);
/*  677:     */     
/*  678:     */ 
/*  679:     */ 
/*  680: 946 */     d.writeUInt(largestBufferSize);
/*  681:     */     
/*  682:     */ 
/*  683:     */ 
/*  684:     */ 
/*  685:     */ 
/*  686:     */ 
/*  687:     */ 
/*  688: 954 */     AbstractAVIStream.VideoTrack vt = null;
/*  689: 955 */     for (AbstractAVIStream.Track t : this.tracks) {
/*  690: 956 */       if ((t instanceof AbstractAVIStream.VideoTrack))
/*  691:     */       {
/*  692: 957 */         vt = (AbstractAVIStream.VideoTrack)t;
/*  693: 958 */         break;
/*  694:     */       }
/*  695:     */     }
/*  696: 961 */     Object fmt = vt.videoFormat;
/*  697: 962 */     d.writeUInt(vt == null ? 0 : ((VideoFormat)fmt).getWidth());
/*  698:     */     
/*  699:     */ 
/*  700: 965 */     d.writeUInt(vt == null ? 0 : ((VideoFormat)fmt).getHeight());
/*  701:     */     
/*  702:     */ 
/*  703: 968 */     d.writeUInt(0L);
/*  704: 969 */     d.writeUInt(0L);
/*  705: 970 */     d.writeUInt(0L);
/*  706: 971 */     d.writeUInt(0L);
/*  707: 974 */     for (fmt = this.tracks.iterator(); ((Iterator)fmt).hasNext();)
/*  708:     */     {
/*  709: 974 */       AbstractAVIStream.Track tr = (AbstractAVIStream.Track)((Iterator)fmt).next();
/*  710:     */       
/*  711:     */ 
/*  712:     */ 
/*  713:     */ 
/*  714:     */ 
/*  715:     */ 
/*  716:     */ 
/*  717:     */ 
/*  718:     */ 
/*  719:     */ 
/*  720:     */ 
/*  721:     */ 
/*  722:     */ 
/*  723:     */ 
/*  724:     */ 
/*  725:     */ 
/*  726:     */ 
/*  727:     */ 
/*  728:     */ 
/*  729:     */ 
/*  730:     */ 
/*  731:     */ 
/*  732:     */ 
/*  733:     */ 
/*  734:     */ 
/*  735:     */ 
/*  736:     */ 
/*  737:     */ 
/*  738:     */ 
/*  739:1004 */       tr.strhChunk.seekToStartOfData();
/*  740:1005 */       d = tr.strhChunk.getOutputStream();
/*  741:1006 */       d.writeType(tr.mediaType.fccType);
/*  742:     */       
/*  743:     */ 
/*  744:     */ 
/*  745:     */ 
/*  746:     */ 
/*  747:     */ 
/*  748:     */ 
/*  749:     */ 
/*  750:     */ 
/*  751:1016 */       d.writeType(tr.fourCC);
/*  752:1022 */       if (((tr instanceof AbstractAVIStream.VideoTrack)) && (((AbstractAVIStream.VideoTrack)tr).videoFormat.getDepth() <= 8)) {
/*  753:1023 */         d.writeUInt(65536L);
/*  754:     */       } else {
/*  755:1025 */         d.writeUInt(0L);
/*  756:     */       }
/*  757:1041 */       d.writeUShort(0);
/*  758:     */       
/*  759:     */ 
/*  760:     */ 
/*  761:     */ 
/*  762:1046 */       d.writeUShort(0);
/*  763:     */       
/*  764:     */ 
/*  765:1049 */       d.writeUInt(0L);
/*  766:     */       
/*  767:     */ 
/*  768:     */ 
/*  769:     */ 
/*  770:     */ 
/*  771:     */ 
/*  772:     */ 
/*  773:1057 */       d.writeUInt(tr.timeScale);
/*  774:     */       
/*  775:     */ 
/*  776:     */ 
/*  777:     */ 
/*  778:     */ 
/*  779:     */ 
/*  780:1064 */       d.writeUInt(tr.frameRate);
/*  781:     */       
/*  782:     */ 
/*  783:1067 */       d.writeUInt(0L);
/*  784:     */       
/*  785:     */ 
/*  786:     */ 
/*  787:     */ 
/*  788:     */ 
/*  789:1073 */       d.writeUInt(tr.samples.size());
/*  790:     */       
/*  791:     */ 
/*  792:     */ 
/*  793:1077 */       long dwSuggestedBufferSize = 0L;
/*  794:1078 */       for (AbstractAVIStream.Sample s : tr.samples) {
/*  795:1079 */         if (s.length > dwSuggestedBufferSize) {
/*  796:1080 */           dwSuggestedBufferSize = s.length;
/*  797:     */         }
/*  798:     */       }
/*  799:1083 */       d.writeUInt(dwSuggestedBufferSize);
/*  800:     */       
/*  801:     */ 
/*  802:     */ 
/*  803:     */ 
/*  804:     */ 
/*  805:1089 */       d.writeInt(-1);
/*  806:     */       
/*  807:     */ 
/*  808:     */ 
/*  809:     */ 
/*  810:     */ 
/*  811:     */ 
/*  812:1096 */       d.writeUInt(0L);
/*  813:     */       
/*  814:     */ 
/*  815:     */ 
/*  816:     */ 
/*  817:     */ 
/*  818:     */ 
/*  819:     */ 
/*  820:     */ 
/*  821:     */ 
/*  822:     */ 
/*  823:1107 */       d.writeUShort((tr instanceof AbstractAVIStream.VideoTrack) ? ((AbstractAVIStream.VideoTrack)tr).rcFrame.x : 0);
/*  824:1108 */       d.writeUShort((tr instanceof AbstractAVIStream.VideoTrack) ? ((AbstractAVIStream.VideoTrack)tr).rcFrame.y : 0);
/*  825:1109 */       d.writeUShort((tr instanceof AbstractAVIStream.VideoTrack) ? ((AbstractAVIStream.VideoTrack)tr).rcFrame.x + ((AbstractAVIStream.VideoTrack)tr).rcFrame.width : 0);
/*  826:1110 */       d.writeUShort((tr instanceof AbstractAVIStream.VideoTrack) ? ((AbstractAVIStream.VideoTrack)tr).rcFrame.y + ((AbstractAVIStream.VideoTrack)tr).rcFrame.height : 0);
/*  827:     */       
/*  828:     */ 
/*  829:     */ 
/*  830:     */ 
/*  831:     */ 
/*  832:     */ 
/*  833:     */ 
/*  834:     */ 
/*  835:     */ 
/*  836:     */ 
					vt = (AbstractAVIStream.VideoTrack)tr;
/*  838:     */       
/*  839:     */ 
/*  840:     */ 
/*  841:     */ 
/*  842:     */ 
/*  843:     */ 
/*  844:     */ 
/*  845:     */ 
/*  846:     */ 
/*  847:     */ 
/*  848:     */ 
/*  849:     */ 
/*  850:     */ 
/*  851:     */ 
/*  852:     */ 
/*  853:     */ 
/*  854:     */ 
/*  855:     */ 
/*  856:1140 */       tr.strfChunk.seekToStartOfData();
/*  857:1141 */       d = tr.strfChunk.getOutputStream();
/*  858:1142 */       d.writeUInt(40L);
/*  859:     */       
/*  860:     */ 
/*  861:     */ 
/*  862:     */ 
/*  863:1147 */       d.writeInt(vt.videoFormat.getWidth());
/*  864:     */       
/*  865:     */ 
/*  866:1150 */       d.writeInt(vt.videoFormat.getHeight());
/*  867:     */       
/*  868:     */ 
/*  869:     */ 
/*  870:     */ 
/*  871:     */ 
/*  872:     */ 
/*  873:     */ 
/*  874:     */ 
/*  875:     */ 
/*  876:     */ 
/*  877:     */ 
/*  878:     */ 
/*  879:     */ 
/*  880:1164 */       d.writeShort(1);
/*  881:     */       
/*  882:     */ 
/*  883:     */ 
/*  884:1168 */       d.writeShort(vt.videoFormat.getDepth());
/*  885:     */       
/*  886:     */ 
/*  887:     */ 
/*  888:     */ 
/*  889:     */ 
/*  890:1174 */       String enc = vt.videoFormat.getEncoding();
/*  891:1175 */       if (enc.equals("DIB ")) {
/*  892:1176 */         d.writeInt(0);
/*  893:1177 */       } else if (enc.equals("RLE "))
/*  894:     */       {
/*  895:1178 */         if (vt.videoFormat.getDepth() == 8) {
/*  896:1179 */           d.writeInt(1);
/*  897:1180 */         } else if (vt.videoFormat.getDepth() == 4) {
/*  898:1181 */           d.writeInt(2);
/*  899:     */         } else {
/*  900:1183 */           throw new UnsupportedOperationException("RLE only supports 4-bit and 8-bit images");
/*  901:     */         }
/*  902:     */       }
/*  903:     */       else {
/*  904:1186 */         d.writeType(vt.videoFormat.getEncoding());
/*  905:     */       }
/*  906:1207 */       if (enc.equals("DIB "))
/*  907:     */       {
/*  908:1208 */         d.writeInt(0);
/*  909:     */       }
/*  910:     */       else
/*  911:     */       {
/*  912:1210 */         VideoFormat fmt1 = vt.videoFormat;
/*  913:1211 */         if (fmt1.getDepth() == 4)
/*  914:     */         {
/*  915:1212 */           d.writeInt(fmt1.getWidth() * fmt1.getHeight() / 2);
/*  916:     */         }
/*  917:     */         else
/*  918:     */         {
/*  919:1214 */           int bytesPerPixel = Math.max(1, fmt1.getDepth() / 8);
/*  920:1215 */           d.writeInt(fmt1.getWidth() * fmt1.getHeight() * bytesPerPixel);
/*  921:     */         }
/*  922:     */       }
/*  923:1222 */       d.writeInt(0);
/*  924:     */       
/*  925:     */ 
/*  926:     */ 
/*  927:1226 */       d.writeInt(0);
/*  928:     */       
/*  929:     */ 
/*  930:     */ 
/*  931:1230 */       d.writeInt(vt.palette == null ? 0 : vt.palette.getMapSize());
/*  932:     */       
/*  933:     */ 
/*  934:     */ 
/*  935:1234 */       d.writeInt(0);
/*  936:1239 */       if (vt.palette != null)
/*  937:     */       {
/*  938:1240 */         int i = 0;
/*  939:1240 */         for (int n = vt.palette.getMapSize(); i < n; i++)
/*  940:     */         {
/*  941:1249 */           d.write(vt.palette.getBlue(i));
/*  942:1250 */           d.write(vt.palette.getGreen(i));
/*  943:1251 */           d.write(vt.palette.getRed(i));
/*  944:1252 */           d.write(0);
/*  945:     */         }
/*  946:     */       }
/*  947:     */     }
/*  948:1258 */     this.aviChunk.finish();
/*  949:     */   }
/*  950:     */ }

