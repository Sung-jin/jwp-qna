package qna.question.repository;

import config.annotation.LocalDataJpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import qna.question.domain.Question;
import qna.question.exception.CannotDeleteException;
import qna.user.domain.User;
import qna.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@LocalDataJpaConfig
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 삭제_상태가_아닌_모든_질문_조회_테스트() {
        List<Question> questions = questionRepository.saveAll(
                Arrays.asList(
                        new Question("title1", "content1"),
                        new Question("title2", "content2"),
                        new Question("title3", "content3"),
                        new Question("title4", "content4")
                )
        );

        List<Question> result = questionRepository.findByDeletedFalse();

        assertThat(result.size()).isGreaterThanOrEqualTo(questions.size());
        for (Question question : questions) {
            assertThat(result.stream().anyMatch(value -> value.getTitle().equals(question.getTitle()))).isTrue();
            assertThat(result.stream().anyMatch(value -> value.getContents().equals(question.getContents()))).isTrue();
        }
    }

    @Test
    void 삭제_상태가_아닌_질문_조회_테스트() {
        Question question = new Question("title", "content");
        Question savedQuestion = questionRepository.save(question);

        Optional<Question> result = questionRepository.findByIdAndDeletedFalse(savedQuestion.getId());

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().isDeleted()).isFalse();
        assertThat(result.get().getTitle()).isEqualTo(savedQuestion.getTitle());
        assertThat(result.get().getContents()).isEqualTo(savedQuestion.getContents());
    }

    @Test
    void 삭제_상태인_질문_조회시_결과가_없어야_한다() throws CannotDeleteException {
        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.save(new User("userId", "password", "name", "email"));
        Question deletedQuestion = new Question("title", "content").writeBy(user);
        Question savedQuestion = questionRepository.save(deletedQuestion);
        savedQuestion.deleteQuestionWithRelatedAnswer(user, now);

        Optional<Question> result = questionRepository.findByIdAndDeletedFalse(savedQuestion.getId());

        assertThat(result.isPresent()).isFalse();
    }
}
