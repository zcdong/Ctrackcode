package Crackcode;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;




public class Main {

private static Map<BufferedImage, String> trainMap = null;
	
	
	 public final static String srcPath = new File("").getAbsolutePath()+"\\srcimg\\";
	 public final static String trainPath = new File("").getAbsolutePath()+"\\trainimg\\";
		
	/**
	 * 下载验证码
	 * @param url
	 * @param imgName  验证码的文件名
	 * @return
	 */
	public static String downloadImage(String url, String imgName) throws MalformedURLException {
		URL murl;
		try {
			murl = new URL(url);
			URLConnection connection=murl.openConnection();
			InputStream is=connection.getInputStream();
			InputStreamReader inputStreamReader=new InputStreamReader(is);
			OutputStream outputStream=new FileOutputStream(new File(srcPath+imgName));
			int length = -1;
			byte[] bytes = new byte[1024];
			while((length = is.read(bytes)) != -1){
				outputStream.write(bytes, 0, length);
			}
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(srcPath+imgName);
		return srcPath+imgName;
	}

	public static int isBlue(int colorInt) {  
        Color color = new Color(colorInt);  
        int rgb = color.getRed() + color.getGreen() + color.getBlue();
        if (rgb == 153) {  
            return 1;  
        }  
        return 0;  
    }  
	
	public static int isBlack(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
			return 1;
		}
		
		return 0;
	}


	public static BufferedImage removeBackgroud(String picFile)
			throws Exception {
		BufferedImage img = ImageIO.read(new File(picFile));  
        img = img.getSubimage(5, 1, img.getWidth()-5, img.getHeight()-2); 
        img = img.getSubimage(0, 0, 50, img.getHeight());
        int width = img.getWidth();  
        int height = img.getHeight();  
        for(int x=0; x<width; x++){
        	for(int y=0; y<height; y++){
        		if(isBlue(img.getRGB(x, y)) == 1){
        			img.setRGB(x, y, Color.BLACK.getRGB());
        		}else{
        			img.setRGB(x, y, Color.WHITE.getRGB());
        		}
        	}
        }
        return img;  
	}
	
	public static List<BufferedImage> splitImage(BufferedImage img)
			throws Exception {
		List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		int width = img.getWidth()/4;
		int height = img.getHeight();		
		subImgs.add(img.getSubimage(0, 0, width, height));
		subImgs.add(img.getSubimage(width, 0, width, height));
		subImgs.add(img.getSubimage(width*2, 0, width, height));
		subImgs.add(img.getSubimage(width*3, 0, width, height));
		return subImgs;
	}

	public static Map<BufferedImage, String> loadTrainData() throws Exception {
		if (trainMap == null) {
			Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
			File dir = new File(trainPath);
			File[] files = dir.listFiles();
			for (File file : files) {
				map.put(ImageIO.read(file), file.getName().charAt(0) + "");
			}
			trainMap = map;
		}
		return trainMap;
	}

	public static String getSingleCharOcr(BufferedImage img,
			Map<BufferedImage, String> map) {
		String result = "#";
		int width = img.getWidth();
		int height = img.getHeight();
		int min = width * height;
		for (BufferedImage bi : map.keySet()) {
			int count = 0;
			
			if (Math.abs(bi.getWidth()-width) > 2)
				continue;
			int widthmin = width < bi.getWidth() ? width : bi.getWidth();
			int heightmin = height < bi.getHeight() ? height : bi.getHeight();
			Label1: for (int x = 0; x < widthmin; ++x) {
				for (int y = 0; y < heightmin; ++y) {
					if (isBlack(img.getRGB(x, y)) != isBlack(bi.getRGB(x, y))) {
						count++;
						if (count >= min)
							break Label1;
					}
				}
			}
			
			if (count < min) {
				min = count;
				result = map.get(bi);
			}
		}
		return result;
	}

	public static String getAllOcr(String file) throws Exception {
		BufferedImage img = removeBackgroud(file);
		
		List<BufferedImage> listImg = splitImage(img);
		Map<BufferedImage, String> map = loadTrainData();
		String result = "";
		for (BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
			
		}
		return result;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(trainPath);
		String pic=downloadImage("http://jwgl.gdut.edu.cn/CheckCode.aspx", "130103021125.png");
		String text = getAllOcr(pic);
		System.out.println("验证码是："+text);

	}
}
