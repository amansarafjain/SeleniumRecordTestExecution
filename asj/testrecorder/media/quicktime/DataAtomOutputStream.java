/*   1:    */ package asj.testrecorder.media.quicktime;
/*   2:    */ 
/*   3:    */ import java.io.FilterOutputStream;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ import java.io.UnsupportedEncodingException;
/*   7:    */ import java.util.Date;
/*   8:    */ import java.util.GregorianCalendar;
/*   9:    */ import javax.imageio.stream.ImageOutputStreamImpl;
/*  10:    */ 
/*  11:    */ public class DataAtomOutputStream
/*  12:    */   extends FilterOutputStream
/*  13:    */ {
/*  14:    */   ImageOutputStreamImpl impl;
/*  15: 28 */   protected static final long MAC_TIMESTAMP_EPOCH = new GregorianCalendar(1904, 0, 1).getTimeInMillis();
/*  16:    */   protected long written;
/*  17:    */   
/*  18:    */   public DataAtomOutputStream(OutputStream out)
/*  19:    */   {
/*  20: 36 */     super(out);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public void writeType(String s)
/*  24:    */     throws IOException
/*  25:    */   {
/*  26: 44 */     if (s.length() != 4) {
/*  27: 45 */       throw new IllegalArgumentException("type string must have 4 characters");
/*  28:    */     }
/*  29:    */     try
/*  30:    */     {
/*  31: 49 */       this.out.write(s.getBytes("ASCII"), 0, 4);
/*  32: 50 */       incCount(4);
/*  33:    */     }
/*  34:    */     catch (UnsupportedEncodingException e)
/*  35:    */     {
/*  36: 52 */       throw new InternalError(e.toString());
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40:    */   public final void writeByte(int v)
/*  41:    */     throws IOException
/*  42:    */   {
/*  43: 66 */     this.out.write(v);
/*  44: 67 */     incCount(1);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public synchronized void write(byte[] b, int off, int len)
/*  48:    */     throws IOException
/*  49:    */   {
/*  50: 85 */     this.out.write(b, off, len);
/*  51: 86 */     incCount(len);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public synchronized void write(int b)
/*  55:    */     throws IOException
/*  56:    */   {
/*  57:103 */     this.out.write(b);
/*  58:104 */     incCount(1);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public void writeInt(int v)
/*  62:    */     throws IOException
/*  63:    */   {
/*  64:117 */     this.out.write(v >>> 24 & 0xFF);
/*  65:118 */     this.out.write(v >>> 16 & 0xFF);
/*  66:119 */     this.out.write(v >>> 8 & 0xFF);
/*  67:120 */     this.out.write(v >>> 0 & 0xFF);
/*  68:121 */     incCount(4);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public void writeUInt(long v)
/*  72:    */     throws IOException
/*  73:    */   {
/*  74:131 */     this.out.write((int)(v >>> 24 & 0xFF));
/*  75:132 */     this.out.write((int)(v >>> 16 & 0xFF));
/*  76:133 */     this.out.write((int)(v >>> 8 & 0xFF));
/*  77:134 */     this.out.write((int)(v >>> 0 & 0xFF));
/*  78:135 */     incCount(4);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public void writeShort(int v)
/*  82:    */     throws IOException
/*  83:    */   {
/*  84:145 */     this.out.write(v >> 8 & 0xFF);
/*  85:146 */     this.out.write(v >>> 0 & 0xFF);
/*  86:147 */     incCount(2);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void writeBCD2(int v)
/*  90:    */     throws IOException
/*  91:    */   {
/*  92:158 */     this.out.write(v % 100 / 10 << 4 | v % 10);
/*  93:159 */     incCount(1);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public void writeBCD4(int v)
/*  97:    */     throws IOException
/*  98:    */   {
/*  99:170 */     this.out.write(v % 10000 / 1000 << 4 | v % 1000 / 100);
/* 100:171 */     this.out.write(v % 100 / 10 << 4 | v % 10);
/* 101:172 */     incCount(2);
/* 102:    */   }
/* 103:    */   
/* 104:    */   public void writeMacTimestamp(Date date)
/* 105:    */     throws IOException
/* 106:    */   {
/* 107:181 */     long millis = date.getTime();
/* 108:182 */     long qtMillis = millis - MAC_TIMESTAMP_EPOCH;
/* 109:183 */     long qtSeconds = qtMillis / 1000L;
/* 110:184 */     writeUInt(qtSeconds);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public void writeFixed16D16(double f)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:195 */     double v = f >= 0.0D ? f : -f;
/* 117:    */     
/* 118:197 */     int wholePart = (int)Math.floor(v);
/* 119:198 */     int fractionPart = (int)((v - wholePart) * 65536.0D);
/* 120:199 */     int t = (wholePart << 16) + fractionPart;
/* 121:201 */     if (f < 0.0D) {
/* 122:202 */       t--;
/* 123:    */     }
/* 124:204 */     writeInt(t);
/* 125:    */   }
/* 126:    */   
/* 127:    */   public void writeFixed2D30(double f)
/* 128:    */     throws IOException
/* 129:    */   {
/* 130:215 */     double v = f >= 0.0D ? f : -f;
/* 131:    */     
/* 132:217 */     int wholePart = (int)v;
/* 133:218 */     int fractionPart = (int)((v - wholePart) * 1073741824.0D);
/* 134:219 */     int t = (wholePart << 30) + fractionPart;
/* 135:221 */     if (f < 0.0D) {
/* 136:222 */       t--;
/* 137:    */     }
/* 138:224 */     writeInt(t);
/* 139:    */   }
/* 140:    */   
/* 141:    */   public void writeFixed8D8(float f)
/* 142:    */     throws IOException
/* 143:    */   {
/* 144:235 */     float v = f >= 0.0F ? f : -f;
/* 145:    */     
/* 146:237 */     int wholePart = (int)v;
/* 147:238 */     int fractionPart = (int)((v - wholePart) * 256.0F);
/* 148:239 */     int t = (wholePart << 8) + fractionPart;
/* 149:241 */     if (f < 0.0F) {
/* 150:242 */       t--;
/* 151:    */     }
/* 152:244 */     writeUShort(t);
/* 153:    */   }
/* 154:    */   
/* 155:    */   public void writePString(String s)
/* 156:    */     throws IOException
/* 157:    */   {
/* 158:254 */     if (s.length() > 65535) {
/* 159:255 */       throw new IllegalArgumentException("String too long for PString");
/* 160:    */     }
/* 161:257 */     if ((s.length() != 0) && (s.length() < 256))
/* 162:    */     {
/* 163:258 */       this.out.write(s.length());
/* 164:    */     }
/* 165:    */     else
/* 166:    */     {
/* 167:260 */       this.out.write(0);
/* 168:261 */       writeShort(s.length());
/* 169:    */     }
/* 170:263 */     for (int i = 0; i < s.length(); i++) {
/* 171:264 */       this.out.write(s.charAt(i));
/* 172:    */     }
/* 173:266 */     incCount(1 + s.length());
/* 174:    */   }
/* 175:    */   
/* 176:    */   public void writePString(String s, int length)
/* 177:    */     throws IOException
/* 178:    */   {
/* 179:277 */     if (s.length() > length) {
/* 180:278 */       throw new IllegalArgumentException("String too long for PString of length " + length);
/* 181:    */     }
/* 182:280 */     if ((s.length() != 0) && (s.length() < 256))
/* 183:    */     {
/* 184:281 */       this.out.write(s.length());
/* 185:    */     }
/* 186:    */     else
/* 187:    */     {
/* 188:283 */       this.out.write(0);
/* 189:284 */       writeShort(s.length());
/* 190:    */     }
/* 191:286 */     for (int i = 0; i < s.length(); i++) {
/* 192:287 */       this.out.write(s.charAt(i));
/* 193:    */     }
/* 194:291 */     for (int i = 1 + s.length(); i < length; i++) {
/* 195:292 */       this.out.write(0);
/* 196:    */     }
/* 197:295 */     incCount(length);
/* 198:    */   }
/* 199:    */   
/* 200:    */   public void writeLong(long v)
/* 201:    */     throws IOException
/* 202:    */   {
/* 203:299 */     this.out.write((int)(v >>> 56) & 0xFF);
/* 204:300 */     this.out.write((int)(v >>> 48) & 0xFF);
/* 205:301 */     this.out.write((int)(v >>> 40) & 0xFF);
/* 206:302 */     this.out.write((int)(v >>> 32) & 0xFF);
/* 207:303 */     this.out.write((int)(v >>> 24) & 0xFF);
/* 208:304 */     this.out.write((int)(v >>> 16) & 0xFF);
/* 209:305 */     this.out.write((int)(v >>> 8) & 0xFF);
/* 210:306 */     this.out.write((int)(v >>> 0) & 0xFF);
/* 211:307 */     incCount(8);
/* 212:    */   }
/* 213:    */   
/* 214:    */   public void writeUShort(int v)
/* 215:    */     throws IOException
/* 216:    */   {
/* 217:311 */     this.out.write(v >> 8 & 0xFF);
/* 218:312 */     this.out.write(v >>> 0 & 0xFF);
/* 219:313 */     incCount(2);
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected void incCount(int value)
/* 223:    */   {
/* 224:321 */     long temp = this.written + value;
/* 225:322 */     if (temp < 0L) {
/* 226:323 */       temp = 9223372036854775807L;
/* 227:    */     }
/* 228:325 */     this.written = temp;
/* 229:    */   }
/* 230:    */   
/* 231:    */   public void writeShorts(short[] s, int off, int len)
/* 232:    */     throws IOException
/* 233:    */   {
/* 234:330 */     if ((off < 0) || (len < 0) || (off + len > s.length) || (off + len < 0)) {
/* 235:331 */       throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > s.length!");
/* 236:    */     }
/* 237:334 */     byte[] b = new byte[len * 2];
/* 238:335 */     int boff = 0;
/* 239:336 */     for (int i = 0; i < len; i++)
/* 240:    */     {
/* 241:337 */       short v = s[(off + i)];
/* 242:338 */       b[(boff++)] = ((byte)(v >>> 8));
/* 243:339 */       b[(boff++)] = ((byte)(v >>> 0));
/* 244:    */     }
/* 245:342 */     write(b, 0, len * 2);
/* 246:    */   }
/* 247:    */   
/* 248:    */   public void writeInts(int[] i, int off, int len)
/* 249:    */     throws IOException
/* 250:    */   {
/* 251:347 */     if ((off < 0) || (len < 0) || (off + len > i.length) || (off + len < 0)) {
/* 252:348 */       throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
/* 253:    */     }
/* 254:351 */     byte[] b = new byte[len * 4];
/* 255:352 */     int boff = 0;
/* 256:353 */     for (int j = 0; j < len; j++)
/* 257:    */     {
/* 258:354 */       int v = i[(off + j)];
/* 259:355 */       b[(boff++)] = ((byte)(v >>> 24));
/* 260:356 */       b[(boff++)] = ((byte)(v >>> 16));
/* 261:357 */       b[(boff++)] = ((byte)(v >>> 8));
/* 262:358 */       b[(boff++)] = ((byte)(v >>> 0));
/* 263:    */     }
/* 264:361 */     write(b, 0, len * 4);
/* 265:    */   }
/* 266:    */   
/* 267:363 */   private byte[] byteBuf = new byte[3];
/* 268:    */   
/* 269:    */   public void writeInt24(int v)
/* 270:    */     throws IOException
/* 271:    */   {
/* 272:366 */     this.byteBuf[0] = ((byte)(v >>> 16));
/* 273:367 */     this.byteBuf[1] = ((byte)(v >>> 8));
/* 274:368 */     this.byteBuf[2] = ((byte)(v >>> 0));
/* 275:369 */     write(this.byteBuf, 0, 3);
/* 276:    */   }
/* 277:    */   
/* 278:    */   public void writeInts24(int[] i, int off, int len)
/* 279:    */     throws IOException
/* 280:    */   {
/* 281:374 */     if ((off < 0) || (len < 0) || (off + len > i.length) || (off + len < 0)) {
/* 282:375 */       throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
/* 283:    */     }
/* 284:378 */     byte[] b = new byte[len * 3];
/* 285:379 */     int boff = 0;
/* 286:380 */     for (int j = 0; j < len; j++)
/* 287:    */     {
/* 288:381 */       int v = i[(off + j)];
/* 289:    */       
/* 290:383 */       b[(boff++)] = ((byte)(v >>> 16));
/* 291:384 */       b[(boff++)] = ((byte)(v >>> 8));
/* 292:385 */       b[(boff++)] = ((byte)(v >>> 0));
/* 293:    */     }
/* 294:388 */     write(b, 0, len * 3);
/* 295:    */   }
/* 296:    */   
/* 297:    */   public final long size()
/* 298:    */   {
/* 299:400 */     return this.written;
/* 300:    */   }
/* 301:    */ }


