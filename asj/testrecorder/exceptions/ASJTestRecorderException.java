/*ASJ:   */ package asj.testrecorder.exceptions;
/*  2:   */ 
/*  3:   */ public class ASJTestRecorderException
/*  4:   */   extends Exception
/*  5:   */ {
/*  6:   */   private String message;
/*  7:   */   
/*  8:   */   public ASJTestRecorderException() {}
/*  9:   */   
/* 10:   */   public ASJTestRecorderException(String message)
/* 11:   */   {
/* 12:14 */     this.message = message;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public String toString()
/* 16:   */   {
/* 17:18 */     return "[Test Recorder Exception] " + this.message;
/* 18:   */   }
/* 19:   */ }

