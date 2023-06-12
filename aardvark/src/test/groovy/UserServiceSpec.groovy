import com.darkona.aardvark.AardvarkApplication
import com.darkona.aardvark.domain.Login
import com.darkona.aardvark.repository.UserRepository
import com.darkona.aardvark.service.UserService
import com.darkona.aardvark.util.FileUtil
import com.darkona.aardvark.util.MapperUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

import javax.naming.AuthenticationException

@SpringBootTest(classes = AardvarkApplication.class)
class UserServiceSpec extends Specification {

    @Autowired
    ApplicationContext context

    @Autowired
    UserService userService

    @Autowired
    UserRepository userRepository

    def mapper = new MapperUtil()
    def user = mapper.deserializeUser(FileUtil.readModelJson("correct_user.json"))

    def "Service loads for testing"() {

        expect:
            context.containsBean("userServiceImpl")
            context.containsBean("userRepository")
            userService
            userRepository
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    def "User is created, token generated, password encrypted, timestamps "(){

        when:
            def newUser = userService.signUserUp(user)
        then:
            newUser
            newUser.getToken()
            newUser.getPassword()
            newUser.getPassword() != user.getPlainPassword()
            newUser.getCreated()
            newUser.getLastLogin()
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    def "Return user data when logging in with correct credentials"(){
        given:
            def newUser = userService.signUserUp(user)
        when:
            def login = new Login().setEmail(user.getEmail()).setPassword(user.getPlainPassword())
            def headers = new HashMap<String,String>()
            headers.put("bearer", newUser.getToken())
            def userData = userService.getUserByLoginCredentials(login, headers)
        then: "LastLogin and Token updated"
            userData
            userData.getLastLogin() != newUser.getLastLogin()
            userData.getToken() != newUser.getToken()
            userData.getEmail() == newUser.getEmail()
            userData.getPassword() == newUser.getPassword()
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    def "User login should fail with wrong or expired token"(){
        given:
            userService.signUserUp(user)
        when:
            def login = new Login().setEmail(user.getEmail()).setPassword(user.getPlainPassword())
            def headers = new HashMap<String,String>()
            headers.put("bearer", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ildyb25nIEFhcmR2YXJrIiwiaWF0IjoxNTE2MjM5MDIyfQ.14tGRsPDcsgO2rvHgBtobWpvf308kEtWypBGfNFe5ZM")
            userService.getUserByLoginCredentials(login, headers)
        then: "AuthenticationException should be thrown and it should say wrong token"
            def exception = thrown(AuthenticationException)
            exception.message.contains("Incorrect token")
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    def "User login should fail with wrong password"(){
        given:
            def newUser = userService.signUserUp(user)
        when:
            def login = new Login().setEmail(user.getEmail()).setPassword("badAardv4rk5")
            def headers = new HashMap<String,String>()
            headers.put("bearer", newUser.getToken())
            userService.getUserByLoginCredentials(login, headers)
        then: "AuthenticationException should be thrown"
            def exception = thrown(AuthenticationException)
            exception.message.contains("Wrong password")
    }


    def "Exception is thrown when attempting to duplicate user"(){
        when:
        userService.signUserUp(user)
        userService.signUserUp(user)
        then:
        thrown(DataIntegrityViolationException)
    }
}
