package chankinwing.captcha.decoder.ocr;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

public class ImageFilter {
	private BufferedImage image;

	private int iw, ih;

	private int[] pixels;

	public ImageFilter(BufferedImage image) {
		this.image = image;
		iw = image.getWidth();
		ih = image.getHeight();
		pixels = new int[iw * ih];
	}

	/** 圖像二值化 */
	public BufferedImage changeGrey() {
		PixelGrabber pg = new PixelGrabber(image.getSource(), 0, 0, iw, ih, pixels, 0, iw);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 設定二值化的域值，默認值為100
		int grey = 100;
		// 對圖像進行二值化處理，Alpha值保持不變
		ColorModel cm = ColorModel.getRGBdefault();
		for (int i = 0; i < iw * ih; i++) {
			int red, green, blue;
			int alpha = cm.getAlpha(pixels[i]);
			if (cm.getRed(pixels[i]) > grey) {
				red = 255;
			} else {
				red = 0;
			}

			if (cm.getGreen(pixels[i]) > grey) {
				green = 255;
			} else {
				green = 0;
			}

			if (cm.getBlue(pixels[i]) > grey) {
				blue = 255;
			} else {
				blue = 0;
			}

			pixels[i] = alpha << 24 | red << 16 | green << 8 | blue;
		}
		// 將數組中的像素產生一個圖像
		return ImageIOHelper.imageProducerToBufferedImage(new MemoryImageSource(iw, ih, pixels, 0,
				iw));
	}

	/** 提升清晰度,進行銳化 */
	public BufferedImage sharp() {
		PixelGrabber pg = new PixelGrabber(image.getSource(), 0, 0, iw, ih, pixels, 0, iw);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 像素的中間變量
		int tempPixels[] = new int[iw * ih];
		for (int i = 0; i < iw * ih; i++) {
			tempPixels[i] = pixels[i];
		}
		// 對圖像進行尖銳化處理，Alpha值保持不變
		ColorModel cm = ColorModel.getRGBdefault();
		for (int i = 1; i < ih - 1; i++) {
			for (int j = 1; j < iw - 1; j++) {
				int alpha = cm.getAlpha(pixels[i * iw + j]);

				// 對圖像進行尖銳化
				int red6 = cm.getRed(pixels[i * iw + j + 1]);
				int red5 = cm.getRed(pixels[i * iw + j]);
				int red8 = cm.getRed(pixels[(i + 1) * iw + j]);
				int sharpRed = Math.abs(red6 - red5) + Math.abs(red8 - red5);

				int green5 = cm.getGreen(pixels[i * iw + j]);
				int green6 = cm.getGreen(pixels[i * iw + j + 1]);
				int green8 = cm.getGreen(pixels[(i + 1) * iw + j]);
				int sharpGreen = Math.abs(green6 - green5) + Math.abs(green8 - green5);

				int blue5 = cm.getBlue(pixels[i * iw + j]);
				int blue6 = cm.getBlue(pixels[i * iw + j + 1]);
				int blue8 = cm.getBlue(pixels[(i + 1) * iw + j]);
				int sharpBlue = Math.abs(blue6 - blue5) + Math.abs(blue8 - blue5);

				if (sharpRed > 255) {
					sharpRed = 255;
				}
				if (sharpGreen > 255) {
					sharpGreen = 255;
				}
				if (sharpBlue > 255) {
					sharpBlue = 255;
				}

				tempPixels[i * iw + j] = alpha << 24 | sharpRed << 16 | sharpGreen << 8 | sharpBlue;
			}
		}

		// 將數組中的像素產生一個圖像
		return ImageIOHelper.imageProducerToBufferedImage(new MemoryImageSource(iw, ih, tempPixels,
				0, iw));
	}

	/** 中值濾波 */
	public BufferedImage median() {
		PixelGrabber pg = new PixelGrabber(image.getSource(), 0, 0, iw, ih, pixels, 0, iw);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 對圖像進行中值濾波，Alpha值保持不變
		ColorModel cm = ColorModel.getRGBdefault();
		for (int i = 1; i < ih - 1; i++) {
			for (int j = 1; j < iw - 1; j++) {
				int red, green, blue;
				int alpha = cm.getAlpha(pixels[i * iw + j]);

				// int red2 = cm.getRed(pixels[(i - 1) * iw + j]);
				int red4 = cm.getRed(pixels[i * iw + j - 1]);
				int red5 = cm.getRed(pixels[i * iw + j]);
				int red6 = cm.getRed(pixels[i * iw + j + 1]);
				// int red8 = cm.getRed(pixels[(i + 1) * iw + j]);

				// 水平方向進行中值濾波
				if (red4 >= red5) {
					if (red5 >= red6) {
						red = red5;
					} else {
						if (red4 >= red6) {
							red = red6;
						} else {
							red = red4;
						}
					}
				} else {
					if (red4 > red6) {
						red = red4;
					} else {
						if (red5 > red6) {
							red = red6;
						} else {
							red = red5;
						}
					}
				}

				// int green2 = cm.getGreen(pixels[(i - 1) * iw + j]);
				int green4 = cm.getGreen(pixels[i * iw + j - 1]);
				int green5 = cm.getGreen(pixels[i * iw + j]);
				int green6 = cm.getGreen(pixels[i * iw + j + 1]);
				// int green8 = cm.getGreen(pixels[(i + 1) * iw + j]);

				// 水平方向進行中值濾波
				if (green4 >= green5) {
					if (green5 >= green6) {
						green = green5;
					} else {
						if (green4 >= green6) {
							green = green6;
						} else {
							green = green4;
						}
					}
				} else {
					if (green4 > green6) {
						green = green4;
					} else {
						if (green5 > green6) {
							green = green6;
						} else {
							green = green5;
						}
					}
				}

				// int blue2 = cm.getBlue(pixels[(i - 1) * iw + j]);
				int blue4 = cm.getBlue(pixels[i * iw + j - 1]);
				int blue5 = cm.getBlue(pixels[i * iw + j]);
				int blue6 = cm.getBlue(pixels[i * iw + j + 1]);
				// int blue8 = cm.getBlue(pixels[(i + 1) * iw + j]);

				// 水平方向進行中值濾波
				if (blue4 >= blue5) {
					if (blue5 >= blue6) {
						blue = blue5;
					} else {
						if (blue4 >= blue6) {
							blue = blue6;
						} else {
							blue = blue4;
						}
					}
				} else {
					if (blue4 > blue6) {
						blue = blue4;
					} else {
						if (blue5 > blue6) {
							blue = blue6;
						} else {
							blue = blue5;
						}
					}
				}
				pixels[i * iw + j] = alpha << 24 | red << 16 | green << 8 | blue;
			}
		}

		// 將數組中的像素產生一個圖像
		return ImageIOHelper.imageProducerToBufferedImage(new MemoryImageSource(iw, ih, pixels, 0,
				iw));
	}

	/** 線性灰度變換 */
	public BufferedImage lineGrey() {
		PixelGrabber pg = new PixelGrabber(image.getSource(), 0, 0, iw, ih, pixels, 0, iw);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 對圖像進行進行線性拉伸，Alpha值保持不變
		ColorModel cm = ColorModel.getRGBdefault();
		for (int i = 0; i < iw * ih; i++) {
			int alpha = cm.getAlpha(pixels[i]);
			int red = cm.getRed(pixels[i]);
			int green = cm.getGreen(pixels[i]);
			int blue = cm.getBlue(pixels[i]);

			// 增加了圖像的亮度
			red = (int) (1.1 * red + 30);
			green = (int) (1.1 * green + 30);
			blue = (int) (1.1 * blue + 30);
			if (red >= 255) {
				red = 255;
			}
			if (green >= 255) {
				green = 255;
			}
			if (blue >= 255) {
				blue = 255;
			}
			pixels[i] = alpha << 24 | red << 16 | green << 8 | blue;
		}

		// 將數組中的像素產生一個圖像

		return ImageIOHelper.imageProducerToBufferedImage(new MemoryImageSource(iw, ih, pixels, 0,
				iw));
	}

	/** 轉換為黑白灰度圖 */
	public BufferedImage grayFilter() {
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		return op.filter(image, null);
	}

	/** 平滑縮放 */
	public BufferedImage scaling(double s) {
		AffineTransform tx = new AffineTransform();
		tx.scale(s, s);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(image, null);
	}

	public BufferedImage scale(Float s) {
		int srcW = image.getWidth();
		int srcH = image.getHeight();
		int newW = Math.round(srcW * s);
		int newH = Math.round(srcH * s);
		// 先做水平方向上的伸縮變換
		BufferedImage tmp = new BufferedImage(newW, newH, image.getType());
		Graphics2D g = tmp.createGraphics();
		for (int x = 0; x < newW; x++) {
			g.setClip(x, 0, 1, srcH);
			// 按比例放縮
			g.drawImage(image, x - x * srcW / newW, 0, null);
		}

		// 再做垂直方向上的伸縮變換
		BufferedImage dst = new BufferedImage(newW, newH, image.getType());
		g = dst.createGraphics();
		for (int y = 0; y < newH; y++) {
			g.setClip(0, y, newW, 1);
			// 按比例放縮
			g.drawImage(tmp, 0, y - y * srcH / newH, null);
		}
		return dst;
	}

}