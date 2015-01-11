/*   1:    */ package asj.testrecorder.media.avi;
/*   2:    */ 
/*   3:    */ import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Codec;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.io.ImageOutputStreamAdapter;

/*   7:    */ import java.awt.Rectangle;
/*   8:    */ import java.awt.image.IndexColorModel;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.io.PrintStream;
/*  11:    */ import java.util.ArrayList;
/*  12:    */ import java.util.LinkedList;
import javax.imageio.stream.ImageOutputStream;
/*  14:    */ 
/*  15:    */ public abstract class AbstractAVIStream
/*  16:    */ {
/*  17:    */   protected ImageOutputStream out;
/*  18:    */   protected long streamOffset;
/*  19:    */   
/*  20:    */   protected static enum MediaType
/*  21:    */   {
/*  22: 68 */     AUDIO("auds"),  MIDI("mids"),  TEXT("txts"),  VIDEO("vids");
/*  23:    */     
/*  24:    */     protected String fccType;
/*  25:    */     
/*  26:    */     private MediaType(String fourCC)
/*  27:    */     {
/*  28: 76 */       this.fccType = fourCC;
/*  29:    */     }
/*  30:    */   }
/*  31:    */   
/*  32: 80 */   protected ArrayList<Track> tracks = new ArrayList();
/*  33:    */   
/*  34:    */   protected long getRelativeStreamPosition()
/*  35:    */     throws IOException
/*  36:    */   {
/*  37: 94 */     return this.out.getStreamPosition() - this.streamOffset;
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected void seekRelative(long newPosition)
/*  41:    */     throws IOException
/*  42:    */   {
/*  43:104 */     this.out.seek(newPosition + this.streamOffset);
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected static class Sample
/*  47:    */   {
/*  48:    */     String chunkType;
/*  49:    */     long offset;
/*  50:    */     long length;
/*  51:    */     int duration;
/*  52:    */     boolean isSync;
/*  53:    */     
/*  54:    */     public Sample(String chunkId, int duration, long offset, long length, boolean isSync)
/*  55:    */     {
/*  56:132 */       this.chunkType = chunkId;
/*  57:133 */       this.duration = duration;
/*  58:134 */       this.offset = offset;
/*  59:135 */       this.length = length;
/*  60:136 */       this.isSync = isSync;
/*  61:    */     }
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected abstract class Track
/*  65:    */   {
/*  66:    */     final AbstractAVIStream.MediaType mediaType;
/*  67:155 */     protected long timeScale = 1L;
/*  68:161 */     protected long frameRate = 30L;
/*  69:    */     protected LinkedList<AbstractAVIStream.Sample> samples;
/*  70:171 */     protected int syncInterval = 30;
/*  71:    */     protected String twoCC;
/*  72:    */     protected String fourCC;
/*  73:    */     AbstractAVIStream.FixedSizeDataChunk strhChunk;
/*  74:    */     AbstractAVIStream.FixedSizeDataChunk strfChunk;
/*  75:    */     
/*  76:    */     public Track(int trackIndex, AbstractAVIStream.MediaType mediaType, String fourCC)
/*  77:    */     {
/*  78:176 */       this.mediaType = mediaType;
/*  79:177 */       this.twoCC = ("00" + Integer.toString(trackIndex));
/*  80:178 */       this.twoCC = this.twoCC.substring(this.twoCC.length() - 2);
/*  81:179 */       this.fourCC = fourCC;
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   protected class VideoTrack
/*  86:    */     extends AbstractAVIStream.Track
/*  87:    */   {
/*  88:    */     protected VideoFormat videoFormat;
/*  89:201 */     protected float videoQuality = 0.97F;
/*  90:    */     protected IndexColorModel palette;
/*  91:    */     protected IndexColorModel previousPalette;
/*  92:    */     protected Object previousData;
/*  93:    */     protected Codec codec;
/*  94:    */     protected Buffer outputBuffer;
/*  95:    */     protected Rectangle rcFrame;
/*  96:    */     
/*  97:    */     public VideoTrack(int trackIndex, String fourCC)
/*  98:    */     {
/*  99:214 */       super(trackIndex, AbstractAVIStream.MediaType.VIDEO, fourCC);
/* 100:    */     }
/* 101:    */   }
/* 102:    */   
/* 103:    */   protected abstract class Chunk
/* 104:    */   {
/* 105:    */     protected String chunkType;
/* 106:    */     protected long offset;
/* 107:    */     
/* 108:    */     public Chunk(String chunkType)
/* 109:    */       throws IOException
/* 110:    */     {
/* 111:237 */       this.chunkType = chunkType;
/* 112:238 */       this.offset = AbstractAVIStream.this.getRelativeStreamPosition();
/* 113:    */     }
/* 114:    */     
/* 115:    */     public abstract void finish()
/* 116:    */       throws IOException;
/* 117:    */     
/* 118:    */     public abstract long size();
/* 119:    */   }
/* 120:    */   
/* 121:    */   protected class CompositeChunk
/* 122:    */     extends AbstractAVIStream.Chunk
/* 123:    */   {
/* 124:    */     protected String compositeType;
/* 125:    */     protected LinkedList<AbstractAVIStream.Chunk> children;
/* 126:    */     protected boolean finished;
/* 127:    */     
/* 128:    */     public CompositeChunk(String compositeType, String chunkType)
/* 129:    */       throws IOException
/* 130:    */     {
/* 131:272 */       super(chunkType);
/* 132:273 */       this.compositeType = compositeType;
/* 133:    */       
/* 134:275 */       AbstractAVIStream.this.out.writeLong(0L);
/* 135:276 */       AbstractAVIStream.this.out.writeInt(0);
/* 136:277 */       this.children = new LinkedList();
/* 137:    */     }
/* 138:    */     
/* 139:    */     public void add(AbstractAVIStream.Chunk child)
/* 140:    */       throws IOException
/* 141:    */     {
/* 142:281 */       if (this.children.size() > 0) {
/* 143:282 */         ((AbstractAVIStream.Chunk)this.children.getLast()).finish();
/* 144:    */       }
/* 145:284 */       this.children.add(child);
/* 146:    */     }
/* 147:    */     
/* 148:    */     public void finish()
/* 149:    */       throws IOException
/* 150:    */     {
/* 151:294 */       if (!this.finished)
/* 152:    */       {
/* 153:295 */         if (size() > 4294967295L) {
/* 154:296 */           throw new IOException("CompositeChunk \"" + this.chunkType + "\" is too large: " + size());
/* 155:    */         }
/* 156:299 */         long pointer = AbstractAVIStream.this.getRelativeStreamPosition();
/* 157:300 */         AbstractAVIStream.this.seekRelative(this.offset);
/* 158:    */         
/* 159:    */ 
/* 160:303 */         DataChunkOutputStream headerData = new DataChunkOutputStream(new ImageOutputStreamAdapter(AbstractAVIStream.this.out), false);
/* 161:304 */         headerData.writeType(this.compositeType);
/* 162:305 */         headerData.writeUInt(size() - 8L);
/* 163:306 */         headerData.writeType(this.chunkType);
/* 164:307 */         for (AbstractAVIStream.Chunk child : this.children) {
/* 165:308 */           child.finish();
/* 166:    */         }
/* 167:310 */         AbstractAVIStream.this.seekRelative(pointer);
/* 168:311 */         if (size() % 2L == 1L) {
/* 169:312 */           AbstractAVIStream.this.out.writeByte(0);
/* 170:    */         }
/* 171:314 */         this.finished = true;
/* 172:    */       }
/* 173:    */     }
/* 174:    */     
/* 175:    */     public long size()
/* 176:    */     {
/* 177:320 */       long length = 12L;
/* 178:321 */       for (AbstractAVIStream.Chunk child : this.children) {
/* 179:322 */         length += child.size() + child.size() % 2L;
/* 180:    */       }
/* 181:324 */       return length;
/* 182:    */     }
/* 183:    */   }
/* 184:    */   
/* 185:    */   protected class DataChunk
/* 186:    */     extends AbstractAVIStream.Chunk
/* 187:    */   {
/* 188:    */     protected DataChunkOutputStream data;
/* 189:    */     protected boolean finished;
/* 190:    */     
/* 191:    */     public DataChunk(String name)
/* 192:    */       throws IOException
/* 193:    */     {
/* 194:342 */       super(name);
/* 195:343 */       AbstractAVIStream.this.out.writeLong(0L);
/* 196:344 */       this.data = new DataChunkOutputStream(new ImageOutputStreamAdapter(AbstractAVIStream.this.out), false);
/* 197:    */     }
/* 198:    */     
/* 199:    */     public DataChunkOutputStream getOutputStream()
/* 200:    */     {
/* 201:348 */       if (this.finished) {
/* 202:349 */         throw new IllegalStateException("DataChunk is finished");
/* 203:    */       }
/* 204:351 */       return this.data;
/* 205:    */     }
/* 206:    */     
/* 207:    */     public long getOffset()
/* 208:    */     {
/* 209:359 */       return this.offset;
/* 210:    */     }
/* 211:    */     
/* 212:    */     public void finish()
/* 213:    */       throws IOException
/* 214:    */     {
/* 215:364 */       if (!this.finished)
/* 216:    */       {
/* 217:365 */         long sizeBefore = size();
/* 218:367 */         if (size() > 4294967295L) {
/* 219:368 */           throw new IOException("DataChunk \"" + this.chunkType + "\" is too large: " + size());
/* 220:    */         }
/* 221:371 */         long pointer = AbstractAVIStream.this.getRelativeStreamPosition();
/* 222:372 */         AbstractAVIStream.this.seekRelative(this.offset);
/* 223:    */         
/* 224:    */ 
/* 225:375 */         DataChunkOutputStream headerData = new DataChunkOutputStream(new ImageOutputStreamAdapter(AbstractAVIStream.this.out), false);
/* 226:376 */         headerData.writeType(this.chunkType);
/* 227:377 */         headerData.writeUInt(size() - 8L);
/* 228:378 */         AbstractAVIStream.this.seekRelative(pointer);
/* 229:379 */         if (size() % 2L == 1L) {
/* 230:380 */           AbstractAVIStream.this.out.writeByte(0);
/* 231:    */         }
/* 232:382 */         this.finished = true;
/* 233:383 */         long sizeAfter = size();
/* 234:384 */         if (sizeBefore != sizeAfter) {
/* 235:385 */           System.err.println("size mismatch " + sizeBefore + ".." + sizeAfter);
/* 236:    */         }
/* 237:    */       }
/* 238:    */     }
/* 239:    */     
/* 240:    */     public long size()
/* 241:    */     {
/* 242:392 */       return 8L + this.data.size();
/* 243:    */     }
/* 244:    */   }
/* 245:    */   
/* 246:    */   protected class FixedSizeDataChunk
/* 247:    */     extends AbstractAVIStream.Chunk
/* 248:    */   {
/* 249:    */     protected DataChunkOutputStream data;
/* 250:    */     protected boolean finished;
/* 251:    */     protected long fixedSize;
/* 252:    */     
/* 253:    */     public FixedSizeDataChunk(String chunkType, long fixedSize)
/* 254:    */       throws IOException
/* 255:    */     {
/* 256:411 */       super(chunkType);
/* 257:412 */       this.fixedSize = fixedSize;
/* 258:413 */       this.data = new DataChunkOutputStream(new ImageOutputStreamAdapter(AbstractAVIStream.this.out), false);
/* 259:414 */       this.data.writeType(chunkType);
/* 260:415 */       this.data.writeUInt(fixedSize);
/* 261:416 */       this.data.clearCount();
/* 262:    */       
/* 263:    */ 
/* 264:419 */       byte[] buf = new byte[(int)Math.min(512L, fixedSize)];
/* 265:420 */       long written = 0L;
/* 266:421 */       while (written < fixedSize)
/* 267:    */       {
/* 268:422 */         this.data.write(buf, 0, (int)Math.min(buf.length, fixedSize - written));
/* 269:423 */         written += Math.min(buf.length, fixedSize - written);
/* 270:    */       }
/* 271:425 */       if (fixedSize % 2L == 1L) {
/* 272:426 */         AbstractAVIStream.this.out.writeByte(0);
/* 273:    */       }
/* 274:428 */       seekToStartOfData();
/* 275:    */     }
/* 276:    */     
/* 277:    */     public DataChunkOutputStream getOutputStream()
/* 278:    */     {
/* 279:435 */       return this.data;
/* 280:    */     }
/* 281:    */     
/* 282:    */     public long getOffset()
/* 283:    */     {
/* 284:443 */       return this.offset;
/* 285:    */     }
/* 286:    */     
/* 287:    */     public void seekToStartOfData()
/* 288:    */       throws IOException
/* 289:    */     {
/* 290:447 */       AbstractAVIStream.this.seekRelative(this.offset + 8L);
/* 291:448 */       this.data.clearCount();
/* 292:    */     }
/* 293:    */     
/* 294:    */     public void seekToEndOfChunk()
/* 295:    */       throws IOException
/* 296:    */     {
/* 297:452 */       AbstractAVIStream.this.seekRelative(this.offset + 8L + this.fixedSize + this.fixedSize % 2L);
/* 298:    */     }
/* 299:    */     
/* 300:    */     public void finish()
/* 301:    */       throws IOException
/* 302:    */     {
/* 303:457 */       if (!this.finished) {
/* 304:458 */         this.finished = true;
/* 305:    */       }
/* 306:    */     }
/* 307:    */     
/* 308:    */     public long size()
/* 309:    */     {
/* 310:464 */       return 8L + this.fixedSize;
/* 311:    */     }
/* 312:    */   }
/* 313:    */ }

