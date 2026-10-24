package com.terridb.chorequest.services;

import com.terridb.chorequest.clients.Auth0ManagementClient;
import com.terridb.chorequest.dtos.users.MeRequestDto;
import com.terridb.chorequest.dtos.users.UserPatchDto;
import com.terridb.chorequest.dtos.users.UserResponseDto;
import com.terridb.chorequest.entities.HouseholdMember;
import com.terridb.chorequest.entities.User;
import com.terridb.chorequest.entities.UserAvatar;
import com.terridb.chorequest.exceptions.AccessDeniedException;
import com.terridb.chorequest.exceptions.BadRequestException;
import com.terridb.chorequest.exceptions.RecordNotFoundException;
import com.terridb.chorequest.mappers.UserMapper;
import com.terridb.chorequest.repositories.HouseholdInviteRepository;
import com.terridb.chorequest.repositories.HouseholdMemberRepository;
import com.terridb.chorequest.repositories.UserAvatarRepository;
import com.terridb.chorequest.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdInviteRepository householdInviteRepository;
    private final Auth0ManagementClient auth0ManagementClient;
    private final UserAvatarRepository userAvatarRepository;
    private static final int AVATAR_SIZE = 256;
    private static final int MAX_SOURCE_DIMENSION = 8000;

    public UserService(UserRepository userRepository, UserMapper userMapper, HouseholdMemberRepository householdMemberRepository, HouseholdInviteRepository householdInviteRepository, Auth0ManagementClient auth0ManagementClient, UserAvatarRepository userAvatarRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.householdMemberRepository = householdMemberRepository;
        this.householdInviteRepository = householdInviteRepository;
        this.auth0ManagementClient = auth0ManagementClient;
        this.userAvatarRepository = userAvatarRepository;
    }

    public UserResponseDto getUserById(UUID id, String sub) {
        User user = getUser(id);
        requireSelf(user, sub);

        return userMapper.toDto(user);
    }

    public UserResponseDto updateUser(UUID id, UserPatchDto input, String sub) {
        User user = getUser(id);
        requireSelf(user, sub);
        User patchedUser = userMapper.patch(input, user);

        return userMapper.toDto(userRepository.save(patchedUser));
    }

    @Transactional
    public void deleteUser(UUID id, String sub) {
        User user = getUser(id);
        requireSelf(user, sub);

        if (user.getMemberships().stream().anyMatch(HouseholdMember::isActive)) {
            throw new BadRequestException("Cannot delete a user who is still a member of a household");
        }

        List<HouseholdMember> pastMemberships = List.copyOf(user.getMemberships());
        pastMemberships.forEach(membership -> membership.setUser(null));
        householdMemberRepository.saveAll(pastMemberships);

        householdInviteRepository.deleteAllByCreatedBy(user);
        userRepository.delete(user);
        userRepository.flush();

        auth0ManagementClient.deleteUser(user.getSub());
    }

    @Transactional
    public UserResponseDto uploadAvatar(UUID id, MultipartFile file, String sub) {
        User user = getUser(id);
        requireSelf(user, sub);

        UserAvatar avatar = userAvatarRepository.findById(id).orElseGet(UserAvatar::new);
        avatar.setUserId(id);
        avatar.setContentType("image/jpeg");
        avatar.setData(resizeToJpeg(file));
        userAvatarRepository.save(avatar);

        user.setCustomAvatarUpdatedAt(LocalDateTime.now());
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto deleteAvatar(UUID id, String sub) {
        User user = getUser(id);
        requireSelf(user, sub);

        userAvatarRepository.deleteById(user.getId());
        user.setCustomAvatarUpdatedAt(null);

        return userMapper.toDto(userRepository.save(user));
    }

    public UserAvatar getAvatar(UUID id) {
        return userAvatarRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Avatar for user " + id + " not found"));
    }

    private byte[] resizeToJpeg(MultipartFile file) {
        BufferedImage source = readImage(file);

        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        int target = Math.min(side, AVATAR_SIZE);

        BufferedImage output = new BufferedImage(target, target, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = output.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, target, target);
        g2d.drawImage(source, 0, 0, target, target, x, y, x + side, y + side, null);
        g2d.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(output, "jpg", out);
        } catch (IOException e) {
            throw new IllegalStateException("Could not encode avatar", e);
        }
        return out.toByteArray();
    }

    private BufferedImage readImage(MultipartFile file) {
        try (InputStream raw = file.getInputStream();
             ImageInputStream in = ImageIO.createImageInputStream(raw)) {
            Iterator<ImageReader> readers = in == null ? Collections.emptyIterator() : ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new BadRequestException("Avatar must be a JPEG or PNG image");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width > MAX_SOURCE_DIMENSION || height > MAX_SOURCE_DIMENSION) {
                    throw new BadRequestException("Avatar must be at most " + MAX_SOURCE_DIMENSION + " pixels wide and high");
                }

                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded file");
        }
    }

    private void requireSelf(User user, String sub) {
        if (!user.getSub().equals(sub)) {
            throw new AccessDeniedException("Cannot access another user's account");
        }
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("User with id " + id + " not found"));
    }

    public UserResponseDto getOrCreateCurrentUser(String sub, MeRequestDto profile) {
        User user = userRepository.findBySub(sub)
                .orElseGet(() -> userRepository.save(buildNewUser(sub, profile)));

        return userMapper.toDto(user);
    }

    public User getUserBySub(String sub) {
        return userRepository.findBySub(sub).
                orElseThrow(() -> new RecordNotFoundException("User with sub " + sub + " not found"));
    }

    private User buildNewUser(String sub, MeRequestDto profile) {
        User user = new User();
        user.setSub(sub);
        user.setFirstName(firstNonBlank(profile.getFirstName(), "New"));
        user.setLastName(firstNonBlank(profile.getLastName(), "User"));
        user.setAvatarUrl(profile.getAvatarUrl());

        return user;
    }

    private String firstNonBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
