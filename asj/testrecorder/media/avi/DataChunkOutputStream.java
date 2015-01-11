/*   1:    */ package asj.testrecorder.media.avi;
/*   2:    */ 
/*   3:    */ import java.io.FilterOutputStream;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ import java.io.UnsupportedEncodingException;
/*   7:    */ 
/*   8:    */ public class DataChunkOutputStream
/*   9:    */   extends FilterOutputStream
/*  10:    */ {
/*  11:    */   protected long written;
/*  12:    */   private boolean forwardFlushAndClose;
/*  13:    */   
/*  14:    */   public DataChunkOutputStream(OutputStream out)
/*  15:    */   {
/*  16: 33 */     this(out, true);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public DataChunkOutputStream(OutputStream out, boolean forwardFlushAndClose)
/*  20:    */   {
/*  21: 37 */     super(out);
/*  22: 38 */     this.forwardFlushAndClose = forwardFlushAndClose;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public void writeType(String s)
/*  26:    */     throws IOException
/*  27:    */   {
/*  28: 46 */     if (s.length() != 4) {
/*  29: 47 */       throw new IllegalArgumentException("type string must have 4 characters");
/*  30:    */     }
/*  31:    */     try
/*  32:    */     {
/*  33: 51 */       this.out.write(s.getBytes("ASCII"), 0, 4);
/*  34: 52 */       incCount(4);
/*  35:    */     }
/*  36:    */     catch (UnsupportedEncodingException e)
/*  37:    */     {
/*  38: 54 */       throw new InternalError(e.toString());
/*  39:    */     }
/*  40:    */   }
/*  41:    */   
/*  42:    */   public final void writeByte(int v)
/*  43:    */     throws IOException
/*  44:    */   {
/*  45: 68 */     this.out.write(v);
/*  46: 69 */     incCount(1);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public synchronized void write(byte[] b, int off, int len)
/*  50:    */     throws IOException
/*  51:    */   {
/*  52: 87 */     this.out.write(b, off, len);
/*  53: 88 */     incCount(len);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public synchronized void write(int b)
/*  57:    */     throws IOException
/*  58:    */   {
/*  59:105 */     this.out.write(b);
/*  60:106 */     incCount(1);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public void writeInt(int v)
/*  64:    */     throws IOException
/*  65:    */   {
/*  66:119 */     this.out.write(v >>> 0 & 0xFF);
/*  67:120 */     this.out.write(v >>> 8 & 0xFF);
/*  68:121 */     this.out.write(v >>> 16 & 0xFF);
/*  69:122 */     this.out.write(v >>> 24 & 0xFF);
/*  70:123 */     incCount(4);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void writeUInt(long v)
/*  74:    */     throws IOException
/*  75:    */   {
/*  76:133 */     this.out.write((int)(v >>> 0 & 0xFF));
/*  77:134 */     this.out.write((int)(v >>> 8 & 0xFF));
/*  78:135 */     this.out.write((int)(v >>> 16 & 0xFF));
/*  79:136 */     this.out.write((int)(v >>> 24 & 0xFF));
/*  80:137 */     incCount(4);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public void writeShort(int v)
/*  84:    */     throws IOException
/*  85:    */   {
/*  86:147 */     this.out.write(v >>> 0 & 0xFF);
/*  87:148 */     this.out.write(v >> 8 & 0xFF);
/*  88:149 */     incCount(2);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public void writeShorts(short[] v, int off, int len)
/*  92:    */     throws IOException
/*  93:    */   {
/*  94:159 */     for (int i = off; i < off + len; i++)
/*  95:    */     {
/*  96:160 */       this.out.write(v[i] >>> 0 & 0xFF);
/*  97:161 */       this.out.write(v[i] >> 8 & 0xFF);
/*  98:    */     }
/*  99:163 */     incCount(len * 2);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void writeInts24(int[] v, int off, int len)
/* 103:    */     throws IOException
/* 104:    */   {
/* 105:173 */     for (int i = off; i < off + len; i++)
/* 106:    */     {
/* 107:174 */       this.out.write(v[i] >>> 0 & 0xFF);
/* 108:175 */       this.out.write(v[i] >> 8 & 0xFF);
/* 109:176 */       this.out.write(v[i] >> 16 & 0xFF);
/* 110:    */     }
/* 111:178 */     incCount(len * 3);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public void writeLong(long v)
/* 115:    */     throws IOException
/* 116:    */   {
/* 117:181 */     this.out.write((int)(v >>> 0) & 0xFF);
/* 118:182 */     this.out.write((int)(v >>> 8) & 0xFF);
/* 119:183 */     this.out.write((int)(v >>> 16) & 0xFF);
/* 120:184 */     this.out.write((int)(v >>> 24) & 0xFF);
/* 121:185 */     this.out.write((int)(v >>> 32) & 0xFF);
/* 122:186 */     this.out.write((int)(v >>> 40) & 0xFF);
/* 123:187 */     this.out.write((int)(v >>> 48) & 0xFF);
/* 124:188 */     this.out.write((int)(v >>> 56) & 0xFF);
/* 125:189 */     incCount(8);
/* 126:    */   }
/* 127:    */   
/* 128:    */   public void writeUShort(int v)
/* 129:    */     throws IOException
/* 130:    */   {
/* 131:193 */     this.out.write(v >>> 0 & 0xFF);
/* 132:194 */     this.out.write(v >> 8 & 0xFF);
/* 133:195 */     incCount(2);
/* 134:    */   }
/* 135:    */   
/* 136:    */   protected void incCount(int value)
/* 137:    */   {
/* 138:203 */     long temp = this.written + value;
/* 139:204 */     if (temp < 0L) {
/* 140:205 */       temp = 9223372036854775807L;
/* 141:    */     }
/* 142:207 */     this.written = temp;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public final long size()
/* 146:    */   {
/* 147:219 */     return this.written;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public void clearCount()
/* 151:    */   {
/* 152:226 */     this.written = 0L;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public void close()
/* 156:    */     throws IOException
/* 157:    */   {
/* 158:231 */     if (this.forwardFlushAndClose) {
/* 159:232 */       super.close();
/* 160:    */     }
/* 161:    */   }
/* 162:    */   
/* 163:    */   public void flush()
/* 164:    */     throws IOException
/* 165:    */   {
/* 166:238 */     if (this.forwardFlushAndClose) {
/* 167:239 */       super.flush();
/* 168:    */     }
/* 169:    */   }
/* 170:    */ }

