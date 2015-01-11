/*   1:    */ package asj.testrecorder.media.io;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.OutputStream;
/*   5:    */ import javax.imageio.stream.ImageOutputStream;
/*   6:    */ 
/*   7:    */ public class ImageOutputStreamAdapter
/*   8:    */   extends OutputStream
/*   9:    */ {
/*  10:    */   protected ImageOutputStream out;
/*  11:    */   
/*  12:    */   public ImageOutputStreamAdapter(ImageOutputStream out)
/*  13:    */   {
/*  14: 41 */     this.out = out;
/*  15:    */   }
/*  16:    */   
/*  17:    */   public void write(int b)
/*  18:    */     throws IOException
/*  19:    */   {
/*  20: 58 */     this.out.write(b);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public void write(byte[] b)
/*  24:    */     throws IOException
/*  25:    */   {
/*  26: 79 */     write(b, 0, b.length);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public void write(byte[] b, int off, int len)
/*  30:    */     throws IOException
/*  31:    */   {
/*  32:104 */     this.out.write(b, off, len);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public void flush()
/*  36:    */     throws IOException
/*  37:    */   {
/*  38:119 */     this.out.flush();
/*  39:    */   }
/*  40:    */   
/*  41:    */   public void close()
/*  42:    */     throws IOException
/*  43:    */   {
/*  44:    */     try
/*  45:    */     {
/*  46:137 */       flush();
/*  47:    */     }
/*  48:    */     finally
/*  49:    */     {
/*  50:139 */       this.out.close();
/*  51:    */     }
/*  52:    */   }
/*  53:    */ }

