package vn.somehouse;
import org.springframework.stereotype.Controller;import org.springframework.web.bind.annotation.GetMapping;
@Controller public class SpaController {
 @GetMapping({"/","/login","/login/","/quan-tri/**","/san-pham/**","/du-an/**","/tin-tuc/**","/ve-chung-toi/**","/ky-gui/**","/cho-thue/**","/quy-nhon/**","/nha-trang/**","/da-nang/**","/en/**","/ru/**","/san-pham-da-luu/**","/chinh-sach-bao-mat/**","/dieu-khoan-su-dung/**","/chinh-sach-cookie/**","/quy-dinh-ky-gui/**","/giai-quyet-khieu-nai/**","/mien-tru-trach-nhiem/**"}) public String spa(){return "forward:/index.html";}
}
