import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.DummyTell;

/**
 * 2 * @Author: flyingjack
 * 3 * @Date: 2021/7/11 9:09 pm
 * 4
 */
public class SimuTell implements DummyTell {
    public String result;


    @Override
    public void tell(ObjectNode message) {
        this.result = message.asText();
    }
}
