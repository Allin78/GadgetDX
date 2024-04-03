package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;


import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Watchface.WatchfaceDeviceParams;

public class HuaweiWatchfaceManager
{

    public static class Resolution {

        Map<String, Object> map = new HashMap<>();
        public Resolution() {
            map.put("HWHD09", "466*466");
            map.put("HWHD08", "320*320");
            map.put("HWHD10", "360*320");
            map.put("HWHD02", "454*454");
            map.put("HWHD01", "390*390");
            map.put("HWHD05", "460*188");
            map.put("HWHD03", "240*120");
            map.put("HWHD04", "160*80");
            map.put("HWHD06", "456*280");
            map.put("HWHD07", "368*194");
        }

        public boolean  isValid(String themeVersion, String screenResolution) {
            String screen = map.get(themeVersion).toString();
            if (screenResolution.equals(screen)) {
                return true;
            } else {
                return false;
            }
        }

        public String screenByThemeVersion(String themeVersion) {
            String screen = map.get(themeVersion).toString();
            return screen;
        }

    }

    public static class WatchfaceDescription {

        public String title;
        public String title_cn;
        public String author;
        public String designer;
        public String screen;
        public String version;
        public String font;
        public String font_cn;

        public WatchfaceDescription(String xmlStr) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(
                        xmlStr)));

                this.title = doc.getElementsByTagName("title").item(0).getTextContent();
                this.title_cn = doc.getElementsByTagName("title-cn").item(0).getTextContent();
                this.author = doc.getElementsByTagName("author").item(0).getTextContent();
                this.designer = doc.getElementsByTagName("designer").item(0).getTextContent();
                this.screen = doc.getElementsByTagName("screen").item(0).getTextContent();
                this.version = doc.getElementsByTagName("version").item(0).getTextContent();
                this.font = doc.getElementsByTagName("font").item(0).getTextContent();
                this.font_cn = doc.getElementsByTagName("font-cn").item(0).getTextContent();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private WatchfaceDeviceParams params;

    public HuaweiWatchfaceManager() {

    }
    public void setParams(WatchfaceDeviceParams params) {
        this.params = params;
    }

    public short getWidth() {
        return params.width;
    }

    public short getHeight() {
        return params.height;
    }


}
