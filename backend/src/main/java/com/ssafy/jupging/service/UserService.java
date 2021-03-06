package com.ssafy.jupging.service;

import com.ssafy.jupging.domain.entity.User;
import com.ssafy.jupging.domain.repository.UserRepository;
import com.ssafy.jupging.dto.UserLoginRequestDto;
import com.ssafy.jupging.dto.UserUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    /*
    로그인시 디비 확인하기
     */
    @Transactional
    public User login(UserLoginRequestDto userLoginRequestDto) {
        User user = userRepository.findByEmailAndPassword(userLoginRequestDto.getEmail(), userLoginRequestDto.getPassword());
        return user;
    }

    /*
    이메일 중복 확인
     */
    @Transactional
    public boolean checkEmail(String email) {
        Optional<User> findEmail = Optional.ofNullable(userRepository.findByEmail(email));
        if (findEmail.isPresent()) {    //이메일 존재, 중복 O
            return false;
        } else {                        //중복 X
            return true;
        }
    }

    /*
    닉네임 중복 확인
     */
    public boolean checkNickname(String nickname) {
        Optional<User> findNickname = Optional.ofNullable(userRepository.findByNickname(nickname));
        if (findNickname.isPresent()) {     //닉네임 존재, 중복 O
            return false;
        } else {
            return true;                   //중복 X
        }
    }

    /*
    회원가입 유저 저장
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    /*
    유저정보찾기
     */
    @Transactional
    public User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저 정보가 없습니다."));
    }

    @Transactional
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    /*
    유저정보수정
     */
    @Transactional
    public void updateUser(Long userId, UserUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저 정보가 없습니다."));
        user.updateUser(requestDto);
    }

    @Transactional
    public void updateFollowing(Long userId, boolean isFollowing) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저 정보가 없습니다."));
        user.updateFollowing(isFollowing);
    }

    @Transactional
    public void updateFollower(Long followUserId, boolean isFollower) {
        User user = userRepository.findById(followUserId).orElseThrow(() -> new IllegalArgumentException("유저 정보가 없습니다."));
        user.updateFollower(isFollower);
    }

    @Transactional
    public void updateScore(Long userId, int score) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저 정보가 없습니다."));
        user.updateScore(score);
    }

    /*
    회원탈퇴
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저 정보가 없습니다."));
        userRepository.deleteById(userId);
    }

    @Transactional
    public List<User> searchUser(String keyword) {
        List<User> searchList = userRepository.findByNicknameContaining(keyword);
        return searchList;
    }

    @Transactional
    public void changePassword(Long userId, String password) {
        Optional<User> user = userRepository.findById(userId);

        user.ifPresent(user1 -> {
            user1.changePassword(password);
            userRepository.save(user1);
        });
    }
}
