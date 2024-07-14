
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.missioncritical.model.Message;
import org.junit.Test;


import static org.junit.Assert.assertNotNull;

public class HandlerTest {


    @Test
    public void whenJavaSerializedToXmlStr_thenCorrect() throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(new Message());
        assertNotNull(xml);
    }

}
