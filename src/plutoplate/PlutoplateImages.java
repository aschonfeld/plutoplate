package plutoplate;

import java.awt.Image;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class PlutoplateImages {
  private Map<String, Object> imageMap;

  public PlutoplateImages() {
    this.imageMap = new HashMap();
    try {
      URL imageURL = getClass().getResource("/images/pluto.jpg");
      URLConnection imageconn = imageURL.openConnection();

      InputStream imageis = imageconn.getInputStream();
      this.imageMap.put("background", ImageIO.read(imageis));
    } catch (Exception e) {
    }
    this.imageMap.put("motorMoving", new ImageIcon(getClass().getResource("/images/cancel.png")));
    this.imageMap.put("motorStopped", new ImageIcon(getClass().getResource("/images/accept.png")));
  }

  public Image getImage(String key) {
    return (Image) this.imageMap.get(key);
  }

  public ImageIcon getIcon(String key) {
    return (ImageIcon) this.imageMap.get(key);
  }
}