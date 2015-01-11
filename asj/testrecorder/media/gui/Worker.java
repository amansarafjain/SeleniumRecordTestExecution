/*   1:    */ package asj.testrecorder.media.gui;
/*   2:    */ 
/*   3:    */ import javax.swing.SwingUtilities;
/*   4:    */ 
/*   5:    */ public abstract class Worker<T>
/*   6:    */   implements Runnable
/*   7:    */ {
/*   8:    */   private T value;
/*   9:    */   private Throwable error;
/*  10:    */   
/*  11:    */   public final void run()
/*  12:    */   {
/*  13:    */     try
/*  14:    */     {
/*  15: 36 */       setValue(construct());
/*  16:    */     }
/*  17:    */     catch (Throwable e)
/*  18:    */     {
/*  19: 38 */       setError(e);
/*  20: 39 */       SwingUtilities.invokeLater(new Runnable()
/*  21:    */       {
/*  22:    */         public void run()
/*  23:    */         {
/*  24: 43 */           Worker.this.failed(Worker.this.getError());
/*  25: 44 */           Worker.this.finished();
/*  26:    */         }
/*  27: 46 */       });
/*  28: 47 */       return;
/*  29:    */     }
/*  30: 49 */     SwingUtilities.invokeLater(new Runnable()
/*  31:    */     {
/*  32:    */       public void run()
/*  33:    */       {
/*  34: 53 */         Worker.this.done(Worker.this.getValue());
/*  35: 54 */         Worker.this.finished();
/*  36:    */       }
/*  37:    */     });
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected abstract T construct()
/*  41:    */     throws Exception;
/*  42:    */   
/*  43:    */   protected void done(T value) {}
/*  44:    */   
/*  45:    */   protected void failed(Throwable error)
/*  46:    */   {
/*  47: 87 */     error.printStackTrace();
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected void finished() {}
/*  51:    */   
/*  52:    */   public synchronized T getValue()
/*  53:    */   {
/*  54:106 */     return this.value;
/*  55:    */   }
/*  56:    */   
/*  57:    */   private synchronized void setValue(T x)
/*  58:    */   {
/*  59:113 */     this.value = x;
/*  60:    */   }
/*  61:    */   
/*  62:    */   protected synchronized Throwable getError()
/*  63:    */   {
/*  64:121 */     return this.error;
/*  65:    */   }
/*  66:    */   
/*  67:    */   private synchronized void setError(Throwable x)
/*  68:    */   {
/*  69:128 */     this.error = x;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void start()
/*  73:    */   {
/*  74:135 */     new Thread(this).start();
/*  75:    */   }
/*  76:    */ }
