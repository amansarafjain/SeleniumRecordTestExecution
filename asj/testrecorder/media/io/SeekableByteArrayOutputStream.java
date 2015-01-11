/*   1:    */ package asj.testrecorder.media.io;
/*   2:    */ 
/*   3:    */ import java.io.ByteArrayOutputStream;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ import java.util.Arrays;
/*   7:    */ 
/*   8:    */ public class SeekableByteArrayOutputStream
/*   9:    */   extends ByteArrayOutputStream
/*  10:    */ {
/*  11:    */   private int pos;
/*  12:    */   
/*  13:    */   public SeekableByteArrayOutputStream()
/*  14:    */   {
/*  15: 36 */     this(32);
/*  16:    */   }
/*  17:    */   
/*  18:    */   public SeekableByteArrayOutputStream(int size)
/*  19:    */   {
/*  20: 47 */     if (size < 0) {
/*  21: 48 */       throw new IllegalArgumentException("Negative initial size: " + 
/*  22: 49 */         size);
/*  23:    */     }
/*  24: 51 */     this.buf = new byte[size];
/*  25:    */   }
/*  26:    */   
/*  27:    */   public SeekableByteArrayOutputStream(byte[] buf)
/*  28:    */   {
/*  29: 57 */     this.buf = buf;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public synchronized void write(int b)
/*  33:    */   {
/*  34: 67 */     int newcount = Math.max(this.pos + 1, this.count);
/*  35: 68 */     if (newcount > this.buf.length) {
/*  36: 69 */       this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
/*  37:    */     }
/*  38: 71 */     this.buf[(this.pos++)] = ((byte)b);
/*  39: 72 */     this.count = newcount;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public synchronized void write(byte[] b, int off, int len)
/*  43:    */   {
/*  44: 85 */     if ((off < 0) || (off > b.length) || (len < 0) || 
/*  45: 86 */       (off + len > b.length) || (off + len < 0)) {
/*  46: 87 */       throw new IndexOutOfBoundsException();
/*  47:    */     }
/*  48: 88 */     if (len == 0) {
/*  49: 89 */       return;
/*  50:    */     }
/*  51: 91 */     int newcount = Math.max(this.pos + len, this.count);
/*  52: 92 */     if (newcount > this.buf.length) {
/*  53: 93 */       this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
/*  54:    */     }
/*  55: 95 */     System.arraycopy(b, off, this.buf, this.pos, len);
/*  56: 96 */     this.pos += len;
/*  57: 97 */     this.count = newcount;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public synchronized void reset()
/*  61:    */   {
/*  62:110 */     this.count = 0;
/*  63:111 */     this.pos = 0;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public void seek(long pos)
/*  67:    */     throws IOException
/*  68:    */   {
/*  69:135 */     this.pos = ((int)pos);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public long getStreamPosition()
/*  73:    */     throws IOException
/*  74:    */   {
/*  75:147 */     return this.pos;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void toOutputStream(OutputStream out)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81:155 */     out.write(this.buf, 0, this.count);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public byte[] getBuffer()
/*  85:    */   {
/*  86:160 */     return this.buf;
/*  87:    */   }
/*  88:    */ }

