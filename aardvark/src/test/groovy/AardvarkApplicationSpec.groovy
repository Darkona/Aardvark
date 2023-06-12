import com.darkona.aardvark.AardvarkApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest(classes = AardvarkApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(loader = SpringBootContextLoader, classes = AardvarkApplication.class)
class AardvarkApplicationSpec extends Specification {

    @Autowired
    ApplicationContext context

    def "Test context loads"() {

        expect:
        context != null
        context.containsBean("userController")
        context.containsBean("userServiceImpl")
        context.containsBean("userRepository")
    }

}
