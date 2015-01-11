/*   1:    */ package asj.testrecorder.media;
/*   2:    */ 
/*   3:    */ import java.awt.Graphics2D;
/*   4:    */ import java.awt.image.BufferedImage;
/*   5:    */ import java.awt.image.DataBufferByte;
/*   6:    */ import java.awt.image.DataBufferInt;
/*   7:    */ import java.awt.image.DataBufferShort;
/*   8:    */ import java.awt.image.DataBufferUShort;
/*   9:    */ import java.awt.image.DirectColorModel;
/*  10:    */ import java.awt.image.WritableRaster;
/*  11:    */ import java.io.IOException;
/*  12:    */ import javax.imageio.stream.ImageOutputStream;
/*  13:    */ 
/*  14:    */ public abstract class AbstractVideoCodec
/*  15:    */   extends AbstractCodec
/*  16:    */ {
/*  17:    */   private BufferedImage imgConverter;
/*  18:    */   
/*  19:    */   protected byte[] getIndexed8(Buffer buf)
/*  20:    */   {
/*  21: 29 */     if ((buf.data instanceof byte[])) {
/*  22: 30 */       return (byte[])buf.data;
/*  23:    */     }
/*  24: 32 */     if ((buf.data instanceof BufferedImage))
/*  25:    */     {
/*  26: 33 */       BufferedImage image = (BufferedImage)buf.data;
/*  27: 34 */       if ((image.getRaster().getDataBuffer() instanceof DataBufferByte)) {
/*  28: 35 */         return ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
/*  29:    */       }
/*  30:    */     }
/*  31: 38 */     return null;
/*  32:    */   }
/*  33:    */   
/*  34:    */   protected short[] getRGB15(Buffer buf)
/*  35:    */   {
/*  36: 44 */     if ((buf.data instanceof int[])) {
/*  37: 45 */       return (short[])buf.data;
/*  38:    */     }
/*  39: 47 */     if ((buf.data instanceof BufferedImage))
/*  40:    */     {
/*  41: 48 */       BufferedImage image = (BufferedImage)buf.data;
/*  42: 49 */       if ((image.getColorModel() instanceof DirectColorModel))
/*  43:    */       {
/*  44: 50 */         DirectColorModel dcm = (DirectColorModel)image.getColorModel();
/*  45: 51 */         if ((image.getRaster().getDataBuffer() instanceof DataBufferShort)) {
/*  46: 53 */           return ((DataBufferShort)image.getRaster().getDataBuffer()).getData();
/*  47:    */         }
/*  48: 55 */         if ((image.getRaster().getDataBuffer() instanceof DataBufferUShort)) {
/*  49: 57 */           return ((DataBufferUShort)image.getRaster().getDataBuffer()).getData();
/*  50:    */         }
/*  51:    */       }
/*  52: 60 */       if (this.imgConverter == null)
/*  53:    */       {
/*  54: 61 */         int width = ((VideoFormat)this.outputFormat).getWidth();
/*  55: 62 */         int height = ((VideoFormat)this.outputFormat).getHeight();
/*  56: 63 */         this.imgConverter = new BufferedImage(width, height, 9);
/*  57:    */       }
/*  58: 65 */       Graphics2D g = this.imgConverter.createGraphics();
/*  59: 66 */       g.drawImage(image, 0, 0, null);
/*  60: 67 */       g.dispose();
/*  61: 68 */       return ((DataBufferShort)this.imgConverter.getRaster().getDataBuffer()).getData();
/*  62:    */     }
/*  63: 70 */     return null;
/*  64:    */   }
/*  65:    */   
/*  66:    */   protected int[] getRGB24(Buffer buf)
/*  67:    */   {
/*  68: 75 */     if ((buf.data instanceof int[])) {
/*  69: 76 */       return (int[])buf.data;
/*  70:    */     }
/*  71: 78 */     if ((buf.data instanceof BufferedImage))
/*  72:    */     {
/*  73: 79 */       BufferedImage image = (BufferedImage)buf.data;
/*  74: 80 */       if ((image.getColorModel() instanceof DirectColorModel))
/*  75:    */       {
/*  76: 81 */         DirectColorModel dcm = (DirectColorModel)image.getColorModel();
/*  77: 82 */         if ((dcm.getBlueMask() == 255) && (dcm.getGreenMask() == 65280) && (dcm.getRedMask() == 16711680) && 
/*  78: 83 */           ((image.getRaster().getDataBuffer() instanceof DataBufferInt))) {
/*  79: 84 */           return ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
/*  80:    */         }
/*  81:    */       }
/*  82: 88 */       VideoFormat vf = (VideoFormat)this.outputFormat;
/*  83: 89 */       return image.getRGB(0, 0, vf.getWidth(), vf.getHeight(), null, 0, vf.getWidth());
/*  84:    */     }
/*  85: 91 */     return null;
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected int[] getARGB32(Buffer buf)
/*  89:    */   {
/*  90: 95 */     if ((buf.data instanceof int[])) {
/*  91: 96 */       return (int[])buf.data;
/*  92:    */     }
/*  93: 98 */     if ((buf.data instanceof BufferedImage))
/*  94:    */     {
/*  95: 99 */       BufferedImage image = (BufferedImage)buf.data;
/*  96:100 */       if ((image.getColorModel() instanceof DirectColorModel))
/*  97:    */       {
/*  98:101 */         DirectColorModel dcm = (DirectColorModel)image.getColorModel();
/*  99:102 */         if ((dcm.getBlueMask() == 255) && (dcm.getGreenMask() == 65280) && (dcm.getRedMask() == 16711680) && 
/* 100:103 */           ((image.getRaster().getDataBuffer() instanceof DataBufferInt))) {
/* 101:104 */           return ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
/* 102:    */         }
/* 103:    */       }
/* 104:108 */       VideoFormat vf = (VideoFormat)this.outputFormat;
/* 105:109 */       return image.getRGB(0, 0, vf.getWidth(), vf.getHeight(), null, 0, vf.getWidth());
/* 106:    */     }
/* 107:111 */     return null;
/* 108:    */   }
/* 109:    */   
/* 110:    */   protected BufferedImage getBufferedImage(Buffer buf)
/* 111:    */   {
/* 112:116 */     if ((buf.data instanceof BufferedImage)) {
/* 113:117 */       return (BufferedImage)buf.data;
/* 114:    */     }
/* 115:119 */     return null;
/* 116:    */   }
/* 117:    */   
/* 118:122 */   private byte[] byteBuf = new byte[4];
/* 119:    */   
/* 120:    */   protected void writeInt24(ImageOutputStream out, int v)
/* 121:    */     throws IOException
/* 122:    */   {
/* 123:124 */     this.byteBuf[0] = ((byte)(v >>> 16));
/* 124:125 */     this.byteBuf[1] = ((byte)(v >>> 8));
/* 125:126 */     this.byteBuf[2] = ((byte)(v >>> 0));
/* 126:127 */     out.write(this.byteBuf, 0, 3);
/* 127:    */   }
/* 128:    */   
/* 129:    */   protected void writeInt24LE(ImageOutputStream out, int v)
/* 130:    */     throws IOException
/* 131:    */   {
/* 132:130 */     this.byteBuf[2] = ((byte)(v >>> 16));
/* 133:131 */     this.byteBuf[1] = ((byte)(v >>> 8));
/* 134:132 */     this.byteBuf[0] = ((byte)(v >>> 0));
/* 135:133 */     out.write(this.byteBuf, 0, 3);
/* 136:    */   }
/* 137:    */   
/* 138:    */   protected void writeInts24(ImageOutputStream out, int[] i, int off, int len)
/* 139:    */     throws IOException
/* 140:    */   {
/* 141:138 */     if ((off < 0) || (len < 0) || (off + len > i.length) || (off + len < 0)) {
/* 142:139 */       throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
/* 143:    */     }
/* 144:142 */     byte[] b = new byte[len * 3];
/* 145:143 */     int boff = 0;
/* 146:144 */     for (int j = 0; j < len; j++)
/* 147:    */     {
/* 148:145 */       int v = i[(off + j)];
/* 149:    */       
/* 150:147 */       b[(boff++)] = ((byte)(v >>> 16));
/* 151:148 */       b[(boff++)] = ((byte)(v >>> 8));
/* 152:149 */       b[(boff++)] = ((byte)(v >>> 0));
/* 153:    */     }
/* 154:152 */     out.write(b, 0, len * 3);
/* 155:    */   }
/* 156:    */   
/* 157:    */   protected void writeInts24LE(ImageOutputStream out, int[] i, int off, int len)
/* 158:    */     throws IOException
/* 159:    */   {
/* 160:156 */     if ((off < 0) || (len < 0) || (off + len > i.length) || (off + len < 0)) {
/* 161:157 */       throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
/* 162:    */     }
/* 163:160 */     byte[] b = new byte[len * 3];
/* 164:161 */     int boff = 0;
/* 165:162 */     for (int j = 0; j < len; j++)
/* 166:    */     {
/* 167:163 */       int v = i[(off + j)];
/* 168:164 */       b[(boff++)] = ((byte)(v >>> 0));
/* 169:165 */       b[(boff++)] = ((byte)(v >>> 8));
/* 170:166 */       b[(boff++)] = ((byte)(v >>> 16));
/* 171:    */     }
/* 172:170 */     out.write(b, 0, len * 3);
/* 173:    */   }
/* 174:    */ }


