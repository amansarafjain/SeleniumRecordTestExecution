/*   1:    */ package asj.testrecorder.media.io;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.OutputStream;
/*   5:    */ import java.nio.ByteOrder;
/*   6:    */ import java.util.Arrays;
/*   7:    */ import javax.imageio.stream.ImageOutputStream;
/*   8:    */ import javax.imageio.stream.ImageOutputStreamImpl;
/*   9:    */ 
/*  10:    */ public class ByteArrayImageOutputStream
/*  11:    */   extends ImageOutputStreamImpl
/*  12:    */ {
/*  13:    */   protected byte[] buf;
/*  14:    */   protected int count;
/*  15:    */   private final int arrayOffset;
/*  16:    */   
/*  17:    */   public ByteArrayImageOutputStream()
/*  18:    */   {
/*  19: 64 */     this(16);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public ByteArrayImageOutputStream(int initialCapacity)
/*  23:    */   {
/*  24: 68 */     this(new byte[initialCapacity]);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public ByteArrayImageOutputStream(byte[] buf)
/*  28:    */   {
/*  29: 72 */     this(buf, ByteOrder.BIG_ENDIAN);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ByteArrayImageOutputStream(byte[] buf, ByteOrder byteOrder)
/*  33:    */   {
/*  34: 76 */     this(buf, 0, buf.length, byteOrder);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public ByteArrayImageOutputStream(byte[] buf, int offset, int length, ByteOrder byteOrder)
/*  38:    */   {
/*  39: 80 */     this.buf = buf;
/*  40: 81 */     this.streamPos = offset;
/*  41: 82 */     this.count = Math.min(offset + length, buf.length);
/*  42: 83 */     this.arrayOffset = offset;
/*  43: 84 */     this.byteOrder = byteOrder;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public ByteArrayImageOutputStream(ByteOrder byteOrder)
/*  47:    */   {
/*  48: 88 */     this(new byte[16], byteOrder);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public synchronized int read()
/*  52:    */     throws IOException
/*  53:    */   {
/*  54:106 */     flushBits();
/*  55:107 */     return this.streamPos < this.count ? this.buf[((int)this.streamPos++)] & 0xFF : -1;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public synchronized int read(byte[] b, int off, int len)
/*  59:    */     throws IOException
/*  60:    */   {
/*  61:141 */     flushBits();
/*  62:142 */     if (b == null) {
/*  63:143 */       throw new NullPointerException();
/*  64:    */     }
/*  65:144 */     if ((off < 0) || (len < 0) || (len > b.length - off)) {
/*  66:145 */       throw new IndexOutOfBoundsException();
/*  67:    */     }
/*  68:147 */     if (this.streamPos >= this.count) {
/*  69:148 */       return -1;
/*  70:    */     }
/*  71:150 */     if (this.streamPos + len > this.count) {
/*  72:151 */       len = (int)(this.count - this.streamPos);
/*  73:    */     }
/*  74:153 */     if (len <= 0) {
/*  75:154 */       return 0;
/*  76:    */     }
/*  77:156 */     System.arraycopy(this.buf, (int)this.streamPos, b, off, len);
/*  78:157 */     this.streamPos += len;
/*  79:158 */     return len;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public synchronized long skip(long n)
/*  83:    */   {
/*  84:174 */     if (this.streamPos + n > this.count) {
/*  85:175 */       n = this.count - this.streamPos;
/*  86:    */     }
/*  87:177 */     if (n < 0L) {
/*  88:178 */       return 0L;
/*  89:    */     }
/*  90:180 */     this.streamPos += n;
/*  91:181 */     return n;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public synchronized int available()
/*  95:    */   {
/*  96:195 */     return (int)(this.count - this.streamPos);
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void close() {}
/* 100:    */   
/* 101:    */   public long getStreamPosition()
/* 102:    */     throws IOException
/* 103:    */   {
/* 104:211 */     checkClosed();
/* 105:212 */     return this.streamPos - this.arrayOffset;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void seek(long pos)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:217 */     checkClosed();
/* 112:218 */     flushBits();
/* 113:221 */     if (pos < this.flushedPos) {
/* 114:222 */       throw new IndexOutOfBoundsException("pos < flushedPos!");
/* 115:    */     }
/* 116:225 */     this.streamPos = (pos + this.arrayOffset);
/* 117:    */   }
/* 118:    */   
/* 119:    */   public synchronized void write(int b)
/* 120:    */     throws IOException
/* 121:    */   {
/* 122:235 */     flushBits();
/* 123:236 */     long newcount = Math.max(this.streamPos + 1L, this.count);
/* 124:237 */     if (newcount > 2147483647L) {
/* 125:238 */       throw new IndexOutOfBoundsException(newcount + " > max array size");
/* 126:    */     }
/* 127:240 */     if (newcount > this.buf.length) {
/* 128:241 */       this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, (int)newcount));
/* 129:    */     }
/* 130:243 */     this.buf[((int)this.streamPos++)] = ((byte)b);
/* 131:244 */     this.count = ((int)newcount);
/* 132:    */   }
/* 133:    */   
/* 134:    */   public synchronized void write(byte[] b)
/* 135:    */     throws IOException
/* 136:    */   {
/* 137:254 */     write(b, 0, b.length);
/* 138:    */   }
/* 139:    */   
/* 140:    */   public synchronized void write(byte[] b, int off, int len)
/* 141:    */     throws IOException
/* 142:    */   {
/* 143:267 */     flushBits();
/* 144:268 */     if ((off < 0) || (off > b.length) || (len < 0) || 
/* 145:269 */       (off + len > b.length) || (off + len < 0)) {
/* 146:270 */       throw new IndexOutOfBoundsException();
/* 147:    */     }
/* 148:271 */     if (len == 0) {
/* 149:272 */       return;
/* 150:    */     }
/* 151:274 */     int newcount = Math.max((int)this.streamPos + len, this.count);
/* 152:275 */     if (newcount > this.buf.length) {
/* 153:276 */       this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
/* 154:    */     }
/* 155:278 */     System.arraycopy(b, off, this.buf, (int)this.streamPos, len);
/* 156:279 */     this.streamPos += len;
/* 157:280 */     this.count = newcount;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public void toOutputStream(OutputStream out)
/* 161:    */     throws IOException
/* 162:    */   {
/* 163:288 */     out.write(this.buf, this.arrayOffset, this.count);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public void toImageOutputStream(ImageOutputStream out)
/* 167:    */     throws IOException
/* 168:    */   {
/* 169:296 */     out.write(this.buf, this.arrayOffset, this.count);
/* 170:    */   }
/* 171:    */   
/* 172:    */   public synchronized byte[] toByteArray()
/* 173:    */   {
/* 174:308 */     byte[] copy = new byte[this.count - this.arrayOffset];
/* 175:309 */     System.arraycopy(this.buf, this.arrayOffset, copy, 0, this.count);
/* 176:310 */     return copy;
/* 177:    */   }
/* 178:    */   
/* 179:    */   public byte[] getBuffer()
/* 180:    */   {
/* 181:315 */     return this.buf;
/* 182:    */   }
/* 183:    */   
/* 184:    */   public long length()
/* 185:    */   {
/* 186:320 */     return this.count - this.arrayOffset;
/* 187:    */   }
/* 188:    */   
/* 189:    */   public synchronized void clear()
/* 190:    */   {
/* 191:332 */     this.count = this.arrayOffset;
/* 192:333 */     this.streamPos = this.arrayOffset;
/* 193:    */   }
/* 194:    */ }

