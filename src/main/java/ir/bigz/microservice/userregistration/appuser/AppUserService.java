package ir.bigz.microservice.userregistration.appuser;

import ir.bigz.microservice.userregistration.registration.token.ConfirmationToken;
import ir.bigz.microservice.userregistration.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@AllArgsConstructor
@Slf4j
public class AppUserService implements UserDetailsService {

    private static final String USER_NOT_FOUND_MSG = "user with email %s not found";
    private final AppUserRepository appUserRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(
                        String.format(USER_NOT_FOUND_MSG, email))
        );
    }

    public String signUpUser(AppUser appUser) {
        Optional<AppUser> appUserFindByEmail = appUserRepository.findByEmail(appUser.getEmail());

        if (appUserFindByEmail.isPresent()) {
            if(appUserFindByEmail.get().getEnabled())
                throw new IllegalStateException("email already taken");

            if(checkAppUserEqual(appUserFindByEmail.get(), appUser)){
                ConfirmationToken confirmationToken = createConfirmationToken(appUserFindByEmail.get());
                confirmationTokenService.saveConfirmationToken(confirmationToken);
                log.info(String.format("generate token %s for appUserExist", confirmationToken.getToken()));
                return confirmationToken.getToken();
            }
            else {
                throw new IllegalStateException("another user with this email already taken");
            }
        }

        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);
        appUserRepository.save(appUser);

        ConfirmationToken confirmationToken = createConfirmationToken(appUser);
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return confirmationToken.getToken();
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }

    private ConfirmationToken createConfirmationToken(AppUser appUser){
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );
        return confirmationToken;
    }

    private boolean checkAppUserEqual(AppUser source, AppUser request){
        return source.userIsSame(request);
    }
}
