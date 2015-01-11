/*   1:    */ package asj.testrecorder.media.quicktime;
/*   2:    */ 
/*   3:    */ import asj.testrecorder.media.AbstractVideoCodec;
import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Format;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.io.SeekableByteArrayOutputStream;

/*   8:    */ import java.awt.Rectangle;
/*   9:    */ import java.awt.image.BufferedImage;
/*  10:    */ import java.awt.image.SampleModel;
/*  11:    */ import java.awt.image.WritableRaster;
/*  12:    */ import java.io.IOException;
import java.io.OutputStream;
/*  14:    */ 
/*  15:    */ public class RawCodec
/*  16:    */   extends AbstractVideoCodec
/*  17:    */ {
/*  18:    */   public Format setInputFormat(Format f)
/*  19:    */   {
/*  20: 52 */     if ((f instanceof VideoFormat))
/*  21:    */     {
/*  22: 53 */       VideoFormat vf = (VideoFormat)f;
/*  23: 54 */       int depth = vf.getDepth();
/*  24: 55 */       if (depth <= 8) {
/*  25: 56 */         depth = 8;
/*  26: 57 */       } else if (depth <= 16) {
/*  27: 58 */         depth = 16;
/*  28: 59 */       } else if (depth <= 24) {
/*  29: 60 */         depth = 24;
/*  30:    */       } else {
/*  31: 62 */         depth = 32;
/*  32:    */       }
/*  33: 64 */       if (BufferedImage.class.isAssignableFrom(vf.getDataClass())) {
/*  34: 65 */         return super.setInputFormat(new VideoFormat("image", vf.getDataClass(), vf.getWidth(), vf.getHeight(), depth));
/*  35:    */       }
/*  36:    */     }
/*  37: 68 */     return super.setInputFormat(null);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public Format setOutputFormat(Format f)
/*  41:    */   {
/*  42: 73 */     if ((f instanceof VideoFormat))
/*  43:    */     {
/*  44: 74 */       VideoFormat vf = (VideoFormat)f;
/*  45: 75 */       int depth = vf.getDepth();
/*  46: 76 */       if (depth <= 8) {
/*  47: 77 */         depth = 8;
/*  48: 78 */       } else if (depth <= 16) {
/*  49: 79 */         depth = 16;
/*  50: 80 */       } else if (depth <= 24) {
/*  51: 81 */         depth = 24;
/*  52:    */       } else {
/*  53: 83 */         depth = 32;
/*  54:    */       }
/*  55: 85 */       return super.setOutputFormat(new VideoFormat("raw ", 
/*  56: 86 */         "NONE", Byte[].class, vf.getWidth(), vf.getHeight(), depth));
/*  57:    */     }
/*  58: 88 */     return super.setOutputFormat(null);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public void writeKey8(OutputStream out, byte[] data, int width, int height, int offset, int scanlineStride)
/*  62:    */     throws IOException
/*  63:    */   {
/*  64:104 */     int xy = offset;
/*  65:104 */     for (int ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride) {
/*  66:105 */       out.write(data, xy, width);
/*  67:    */     }
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void writeKey16(OutputStream out, short[] data, int width, int height, int offset, int scanlineStride)
/*  71:    */     throws IOException
/*  72:    */   {
/*  73:122 */     byte[] bytes = new byte[width * 2];
/*  74:123 */     int xy = offset;
/*  75:123 */     for (int ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride)
/*  76:    */     {
/*  77:124 */       int x = 0;
/*  78:124 */       for (int i = 0; x < width; i += 2)
/*  79:    */       {
/*  80:125 */         int pixel = data[(xy + x)];
/*  81:126 */         bytes[i] = ((byte)(pixel >> 8));
/*  82:127 */         bytes[(i + 1)] = ((byte)pixel);x++;
/*  83:    */       }
/*  84:129 */       out.write(bytes, 0, bytes.length);
/*  85:    */     }
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void writeKey24(OutputStream out, int[] data, int width, int height, int offset, int scanlineStride)
/*  89:    */     throws IOException
/*  90:    */   {
/*  91:146 */     byte[] bytes = new byte[width * 3];
/*  92:147 */     int xy = offset;
/*  93:147 */     for (int ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride)
/*  94:    */     {
/*  95:148 */       int x = 0;
/*  96:148 */       for (int i = 0; x < width; i += 3)
/*  97:    */       {
/*  98:149 */         int pixel = data[(xy + x)];
/*  99:150 */         bytes[i] = ((byte)(pixel >> 16));
/* 100:151 */         bytes[(i + 1)] = ((byte)(pixel >> 8));
/* 101:152 */         bytes[(i + 2)] = ((byte)pixel);x++;
/* 102:    */       }
/* 103:154 */       out.write(bytes, 0, bytes.length);
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:    */   public void writeKey32(OutputStream out, int[] data, int width, int height, int offset, int scanlineStride)
/* 108:    */     throws IOException
/* 109:    */   {
/* 110:171 */     byte[] bytes = new byte[width * 4];
/* 111:172 */     int xy = offset;
/* 112:172 */     for (int ymax = offset + height * scanlineStride; xy < ymax; xy += scanlineStride)
/* 113:    */     {
/* 114:173 */       int x = 0;
/* 115:173 */       for (int i = 0; x < width; i += 4)
/* 116:    */       {
/* 117:174 */         int pixel = data[(xy + x)];
/* 118:175 */         bytes[i] = ((byte)(pixel >> 24));
/* 119:176 */         bytes[(i + 1)] = ((byte)(pixel >> 16));
/* 120:177 */         bytes[(i + 2)] = ((byte)(pixel >> 8));
/* 121:178 */         bytes[(i + 3)] = ((byte)pixel);x++;
/* 122:    */       }
/* 123:180 */       out.write(bytes, 0, bytes.length);
/* 124:    */     }
/* 125:    */   }
/* 126:    */   
/* 127:    */   public void writeKey24(OutputStream out, BufferedImage image)
/* 128:    */     throws IOException
/* 129:    */   {
/* 130:196 */     int width = image.getWidth();
/* 131:197 */     int height = image.getHeight();
/* 132:198 */     WritableRaster raster = image.getRaster();
/* 133:199 */     int[] rgb = new int[width * 3];
/* 134:200 */     byte[] bytes = new byte[width * 3];
/* 135:201 */     for (int y = 0; y < height; y++)
/* 136:    */     {
/* 137:203 */       rgb = raster.getPixels(0, y, width, 1, rgb);
/* 138:204 */       int k = 0;
/* 139:204 */       for (int n = width * 3; k < n; k++) {
/* 140:205 */         bytes[k] = ((byte)rgb[k]);
/* 141:    */       }
/* 142:207 */       out.write(bytes);
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   public void process(Buffer in, Buffer out)
/* 147:    */   {
/* 148:214 */     if ((in.flags & 0x2) != 0)
/* 149:    */     {
/* 150:215 */       out.flags = 2;
/* 151:216 */       return;
/* 152:    */     }
/* 153:218 */     out.format = this.outputFormat;
/* 154:    */     SeekableByteArrayOutputStream tmp;
/* 155:    */     
/* 156:221 */     if ((out.data instanceof byte[])) {
/* 157:222 */       tmp = new SeekableByteArrayOutputStream((byte[])out.data);
/* 158:    */     } else {
/* 159:224 */       tmp = new SeekableByteArrayOutputStream();
/* 160:    */     }
/* 161:226 */     VideoFormat vf = (VideoFormat)this.outputFormat;
/* 162:    */     Rectangle r;
/* 163:    */     int scanlineStride;
/* 164:231 */     if ((in.data instanceof BufferedImage))
/* 165:    */     {
/* 166:232 */       BufferedImage image = (BufferedImage)in.data;
/* 167:233 */       WritableRaster raster = image.getRaster();
/* 168:234 */       scanlineStride = raster.getSampleModel().getWidth();
/* 169:235 */       r = raster.getBounds();
/* 170:236 */       r.x -= raster.getSampleModelTranslateX();
/* 171:237 */       r.y -= raster.getSampleModelTranslateY();
/* 172:    */     }
/* 173:    */     else
/* 174:    */     {
/* 175:239 */       r = new Rectangle(0, 0, vf.getWidth(), vf.getHeight());
/* 176:240 */       scanlineStride = vf.getWidth();
/* 177:    */     }
/* 178:    */     try
/* 179:    */     {
/* 180:244 */       switch (vf.getDepth())
/* 181:    */       {
/* 182:    */       case 8: 
/* 183:246 */         writeKey8(tmp, getIndexed8(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
/* 184:247 */         break;
/* 185:    */       case 16: 
/* 186:250 */         writeKey16(tmp, getRGB15(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
/* 187:251 */         break;
/* 188:    */       case 24: 
/* 189:254 */         writeKey24(tmp, getRGB24(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
/* 190:255 */         break;
/* 191:    */       case 32: 
/* 192:258 */         writeKey24(tmp, getARGB32(in), r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);
/* 193:259 */         break;
/* 194:    */       default: 
/* 195:262 */         out.flags = 2;
/* 196:263 */         return;
/* 197:    */       }
/* 198:267 */       out.flags = 16;
/* 199:268 */       out.data = tmp.getBuffer();
/* 200:269 */       out.offset = 0;
/* 201:270 */       out.length = ((int)tmp.getStreamPosition());
/* 202:271 */       return;
/* 203:    */     }
/* 204:    */     catch (IOException ex)
/* 205:    */     {
/* 206:273 */       ex.printStackTrace();
/* 207:274 */       out.flags = 2;
/* 208:    */     }
/* 209:    */   }
/* 210:    */ }

