package com.example.backend.domain.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "sites")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("유적지 ID")
    private UUID id;

    @Column(name = "site_order", nullable = false)
    @Comment("유적지 순서(1부터 시작)")
    private int order;

    @Column(nullable = false, length = 100)
    @Comment("유적지 이름")
    private String name;

    @Column(nullable = false, length = 255)
    @Comment("목록용 한 줄 소개")
    private String shortDescription;

    @Column(nullable = false)
    @Comment("상세 화면 이미지 URL(1장)")
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "text")
    @Comment("유적지 상세 설명 텍스트")
    private String descriptionText;

    @Column(nullable = false)
    @Comment("고정 내레이션 오디오 URL")
    private String narrationAudioUrl;

    @Column(nullable = false)
    @Comment("고정 내레이션 길이(초)")
    private int narrationDurationSec;

    protected Site() {
    }

    public UUID getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public String getNarrationAudioUrl() {
        return narrationAudioUrl;
    }

    public void setNarrationAudioUrl(String narrationAudioUrl) {
        this.narrationAudioUrl = narrationAudioUrl;
    }

    public int getNarrationDurationSec() {
        return narrationDurationSec;
    }

    public void setNarrationDurationSec(int narrationDurationSec) {
        this.narrationDurationSec = narrationDurationSec;
    }
}

