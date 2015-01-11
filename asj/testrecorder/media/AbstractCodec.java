/*  1:   */ package asj.testrecorder.media;
/*  2:   */ 
/*  3:   */ public abstract class AbstractCodec
/*  4:   */   implements Codec
/*  5:   */ {
/*  6:   */   protected Format inputFormat;
/*  7:   */   protected Format outputFormat;
/*  8:17 */   protected float quality = 1.0F;
/*  9:   */   
/* 10:   */   public Format setInputFormat(Format f)
/* 11:   */   {
/* 12:22 */     this.inputFormat = f;
/* 13:23 */     return f;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public Format setOutputFormat(Format f)
/* 17:   */   {
/* 18:28 */     this.outputFormat = f;
/* 19:29 */     return f;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public void setQuality(float newValue)
/* 23:   */   {
/* 24:34 */     this.quality = newValue;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public float getQuality()
/* 28:   */   {
/* 29:39 */     return this.quality;
/* 30:   */   }
/* 31:   */ }

