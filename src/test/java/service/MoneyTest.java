package service;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
class MoneyTest {
    @ParameterizedTest @ValueSource(strings={"0","-1","1.001","1e2","1,000","NaN","99999999999.99",".01","","+1"})
    void rejectsInvalidInputWithoutRounding(String input){assertThrows(ValidationException.class,()->Money.parse(input));}
    @ParameterizedTest @CsvSource({"1,1.00","0.01,0.01","9999999999.99,9999999999.99","0001.2,1.20"})
    void normalizesExactAmounts(String input,String expected)throws Exception{assertEquals(new BigDecimal(expected),Money.parse(input));}
    @Test void rejectsNullAndFractionalCent(){assertThrows(ValidationException.class,()->Money.positive(null));assertThrows(ValidationException.class,()->Money.positive(new BigDecimal("0.001")));}
}
