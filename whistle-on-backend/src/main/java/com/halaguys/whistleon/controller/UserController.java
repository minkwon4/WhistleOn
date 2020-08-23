package com.halaguys.whistleon.controller;

import com.halaguys.whistleon.domain.user.User;
import com.halaguys.whistleon.dto.request.UserLoginRequestDto;
import com.halaguys.whistleon.dto.request.UserRegistRequestDto;
import com.halaguys.whistleon.dto.response.UserInfoResponseDto;
import com.halaguys.whistleon.dto.response.UserStatResponseDto;
import com.halaguys.whistleon.exception.UnauthorizedException;
import com.halaguys.whistleon.service.EmailService;
import com.halaguys.whistleon.service.JwtService;
import com.halaguys.whistleon.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
@CrossOrigin(origins = "*")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @ApiOperation(value = "로그인")
    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto userLoginRequestDto, HttpServletResponse response){
        Map<String,String> map = new HashMap<>();
        try{
            User user = userService.login(userLoginRequestDto);
            String token = jwtService.create(user);
            response.setHeader("Authorization",token);
            map.put("msg","로그인이 성공하였습니다.");
            map.put("userName",user.getUserName());
            return new ResponseEntity<>(map,HttpStatus.OK);
        }catch (NoSuchElementException | UnauthorizedException e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "회원가입")
    @PostMapping("/users/signup")
    public ResponseEntity<?> signUp(@RequestBody UserRegistRequestDto userRegistRequestDto){
        Map<String,String> map = new HashMap<>();
        try{
            userService.regist(userRegistRequestDto);
            map.put("msg","회원가입이 성공하였습니다.");
            return new ResponseEntity<>(map,HttpStatus.CREATED);
        }catch (UnauthorizedException e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "이메일 인증하기")
    @GetMapping("/users/auth/{email}")
    public ResponseEntity<?> authEmail(@PathVariable String email){
        Map<String,String> map = new HashMap<>();
        try{
            if(userService.checkEmail(email)){
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }else{
                emailService.sendMail(email);
                map.put("msg","인증 번호를 발송하였습니다.");
                return new ResponseEntity<>(map,HttpStatus.OK);
            }
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "인증번호 확인하기")
    @GetMapping("/users/auth/{email}/{code}")
    public ResponseEntity<?> authCode(@PathVariable String email, @PathVariable String code){
        Map<String,String> map = new HashMap<>();
        if(emailService.getMailCode(email).equals(code)){
            emailService.deleteMailCode(email);
            map.put("msg","인증이 완료되었습니다.");
            return new ResponseEntity<>(map,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @ApiOperation(value = "마이페이지")
    @GetMapping("/users/{email}")
    public ResponseEntity<? extends UserInfoResponseDto> userInfo(@PathVariable String email){
        try{
            UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(email);
            return new ResponseEntity<>(userInfoResponseDto,HttpStatus.OK);
        }catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "개인스탯")
    @GetMapping("/users/stat/{email}")
    public ResponseEntity<? extends UserStatResponseDto> userStat(@PathVariable String email){
        try{
            UserStatResponseDto userStatResponseDto = userService.getUserStat(email);
            return new ResponseEntity<>(userStatResponseDto,HttpStatus.OK);
        }catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
