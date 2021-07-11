import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.DummyTell;

public class SimuTell implements DummyTell {
    public String result;


    @Override
    public void tell(ObjectNode message) {
        this.result = message.asText();
    }
}
