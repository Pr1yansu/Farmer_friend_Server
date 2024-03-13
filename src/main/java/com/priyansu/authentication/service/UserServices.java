package com.priyansu.authentication.service;

import com.priyansu.authentication.entity.User;
import com.priyansu.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserServices implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User createUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User authenticateUser(String email,String pass){
        try {
            User user = userRepository.findByEmail(email);
            if(Objects.nonNull(user) && passwordEncoder.matches(pass,user.getPassword())){
                return user;
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }

    public boolean updateUserImage(String email,String imageUrl){
        try {
            User user = userRepository.findByEmail(email);
            user.setImage(imageUrl);
            userRepository.save(user);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public User updateUser(User user){
        User existingUser = userRepository.findByEmail(user.getEmail());
        if(existingUser!=null){
           userRepository.delete(existingUser);
           userRepository.save(user);
           return user;
        }
        return null;
    }

    public User profile(String email){
        return userRepository.findByEmail(email);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username);
    }


}
