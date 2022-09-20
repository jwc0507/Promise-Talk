package com.example.week8.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class FriendList {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne (fetch = FetchType.LAZY)
    private Member member;          // 기준이 되는 맴버

    @JoinColumn(name = "friend_id", nullable = false)
    @OneToOne (fetch = FetchType.LAZY)
    private Member friend;          // 기준 맴버의 친구들

//    @JoinColumn
//    @OneToMany
//    private List<Member> friends = new ArrayList<>();
}
