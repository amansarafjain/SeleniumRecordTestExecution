/*  1:   */ package asj.testrecorder.media.color;
/*  2:   */ 
/*  3:   */ import java.awt.image.IndexColorModel;
/*  4:   */ 
/*  5:   */ public class Colors
/*  6:   */ {
/*  7:   */   public static IndexColorModel createMacColors()
/*  8:   */   {
/*  9:41 */     byte[] r = new byte[256];
/* 10:42 */     byte[] g = new byte[256];
/* 11:43 */     byte[] b = new byte[256];
/* 12:   */     
/* 13:   */ 
/* 14:46 */     int index = 0;
/* 15:47 */     for (int i = 0; i < 6; i++) {
/* 16:48 */       for (int j = 0; j < 6; j++) {
/* 17:49 */         for (int k = 0; k < 6; k++)
/* 18:   */         {
/* 19:50 */           r[index] = ((byte)(255 - 51 * i));
/* 20:51 */           g[index] = ((byte)(255 - 51 * j));
/* 21:52 */           b[index] = ((byte)(255 - 51 * k));
/* 22:53 */           index++;
/* 23:   */         }
/* 24:   */       }
/* 25:   */     }
/* 26:58 */     index--;
/* 27:   */     
/* 28:   */ 
/* 29:61 */     byte[] ramp = { -18, -35, -69, -86, -120, 119, 85, 68, 34, 17 };
/* 30:62 */     for (int i = 0; i < 10; i++)
/* 31:   */     {
/* 32:63 */       r[index] = ramp[i];
/* 33:64 */       g[index] = 0;
/* 34:65 */       b[index] = 0;
/* 35:66 */       index++;
/* 36:   */     }
/* 37:69 */     for (int j = 0; j < 10; j++)
/* 38:   */     {
/* 39:70 */       r[index] = 0;
/* 40:71 */       g[index] = ramp[j];
/* 41:72 */       b[index] = 0;
/* 42:73 */       index++;
/* 43:   */     }
/* 44:76 */     for (int k = 0; k < 10; k++)
/* 45:   */     {
/* 46:77 */       r[index] = 0;
/* 47:78 */       g[index] = 0;
/* 48:79 */       b[index] = ramp[k];
/* 49:80 */       index++;
/* 50:   */     }
/* 51:83 */     for (int ijk = 0; ijk < 10; ijk++)
/* 52:   */     {
/* 53:84 */       r[index] = ramp[ijk];
/* 54:85 */       g[index] = ramp[ijk];
/* 55:86 */       b[index] = ramp[ijk];
/* 56:87 */       index++;
/* 57:   */     }
/* 58:97 */     IndexColorModel icm = new IndexColorModel(8, 256, r, g, b);
/* 59:98 */     return icm;
/* 60:   */   }
/* 61:   */ }

