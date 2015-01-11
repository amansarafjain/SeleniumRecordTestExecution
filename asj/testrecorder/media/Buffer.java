/*  1:   */ package asj.testrecorder.media;
/*  2:   */ 
/*  3:   */ public class Buffer
/*  4:   */ {
/*  5:   */   public static final int FLAG_DISCARD = 2;
/*  6:   */   public static final int FLAG_KEY_FRAME = 16;
/*  7:   */   public int flags;
/*  8:   */   public Object data;
/*  9:   */   public int offset;
/* 10:   */   public int length;
/* 11:   */   public long duration;
/* 12:   */   public long timeScale;
/* 13:   */   public long timeStamp;
/* 14:   */   public Format format;
/* 15:50 */   public int sampleCount = 1;
/* 16:   */ }


