CREATE KEYSPACE i18n
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use i18n;

CREATE TABLE languages (country text,
                        language text,
                        country_en text,
                        primary key ((country_en))
);


use i18n;
INSERT INTO languages (country, language, country_en) VALUES ('中国', 'zh-CN', 'China');
INSERT INTO languages (country, language, country_en) VALUES ('United States', 'en-US', 'United States');
INSERT INTO languages (country, language, country_en) VALUES ('United Kingdom', 'en-GB', 'United Kingdom');
INSERT INTO languages (country, language, country_en) VALUES ('France', 'fr-FR', 'France');
INSERT INTO languages (country, language, country_en) VALUES ('Deutschland', 'de-DE', 'Germany');
INSERT INTO languages (country, language, country_en) VALUES ('España', 'es-ES', 'Spain');
INSERT INTO languages (country, language, country_en) VALUES ('Italia', 'it-IT', 'Italy');
INSERT INTO languages (country, language, country_en) VALUES ('日本', 'ja-JP', 'Japan');
INSERT INTO languages (country, language, country_en) VALUES ('대한민국', 'ko-KR', 'South Korea');
INSERT INTO languages (country, language, country_en) VALUES ('Россия', 'ru-RU', 'Russia');
INSERT INTO languages (country, language, country_en) VALUES ('الإمارات العربية المتحدة', 'ar-AE', 'United Arab Emirates');
INSERT INTO languages (country, language, country_en) VALUES ('भारत', 'hi-IN', 'India');
INSERT INTO languages (country, language, country_en) VALUES ('Portugal', 'pt-PT', 'Portugal');
INSERT INTO languages (country, language, country_en) VALUES ('Nederland', 'nl-NL', 'Netherlands');
INSERT INTO languages (country, language, country_en) VALUES ('Sverige', 'sv-SE', 'Sweden');
INSERT INTO languages (country, language, country_en) VALUES ('Suomi', 'fi-FI', 'Finland');
INSERT INTO languages (country, language, country_en) VALUES ('Norge', 'no-NO', 'Norway');
INSERT INTO languages (country, language, country_en) VALUES ('Polska', 'pl-PL', 'Poland');
INSERT INTO languages (country, language, country_en) VALUES ('Türkiye', 'tr-TR', 'Turkey');
INSERT INTO languages (country, language, country_en) VALUES ('Ελλάδα', 'el-GR', 'Greece');
INSERT INTO languages (country, language, country_en) VALUES ('Česká republika', 'cs-CZ', 'Czech Republic');
INSERT INTO languages (country, language, country_en) VALUES ('ישראל', 'he-IL', 'Israel');
INSERT INTO languages (country, language, country_en) VALUES ('ไทย', 'th-TH', 'Thailand');



