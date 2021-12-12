package DataTransferObject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Struct" +
        "NumberMessage")
@XmlRootElement
public class Structure {
    @XmlElement(name = "message", required = false)
    public String message;

    @XmlElement(name = "number", required = true)
    public int number;

    @XmlElement(name = "Flag", required = false)
    public boolean Flag;

    public Structure() {
    }

    public Structure(String message, int number, boolean Flag){
        this.message = message;
        this.number = number;
        this.Flag = Flag;
    }

    @Override
    public String toString() {
        return "Structure{" +
                "message='" + message + '\'' +
                ", number=" + number +
                '}';
    }
}
