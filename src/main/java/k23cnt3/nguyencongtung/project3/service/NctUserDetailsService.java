package k23cnt3.nguyencongtung.project3.service;

import k23cnt3.nguyencongtung.project3.entity.NctUser;
import k23cnt3.nguyencongtung.project3.repository.NctUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class NctUserDetailsService implements UserDetailsService {

    private final NctUserRepository nctUserRepository;

    @Autowired
    public NctUserDetailsService(NctUserRepository nctUserRepository) {
        this.nctUserRepository = nctUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user in the database by their username
        NctUser nctUser = nctUserRepository.findByNctUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với tên đăng nhập: " + username));

        // Create a UserDetails object that Spring Security can use for authentication
        return new org.springframework.security.core.userdetails.User(
                nctUser.getNctUsername(),
                nctUser.getNctPassword(), // The plain text password from the database
                // Add "ROLE_" prefix for standard Spring Security role handling
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + nctUser.getNctRole().name()))
        );
    }
}
