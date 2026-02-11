-- 10条随机测试数据
INSERT INTO public.study_plans (
    id, user_id, title, description, plan_type, subject, 
    difficulty, status, progress_percent, start_date, end_date
) VALUES
(1, 1, 'Mathematics Weekly Review', 'Weekly review of algebra and geometry concepts', 'weekly', 'Mathematics', 'medium', 'active', 25, '2024-01-01', '2024-01-31'),
(2, 2, 'English Vocabulary Building', 'Daily vocabulary practice with 10 new words', 'daily', 'English', 'easy', 'active', 60, '2024-01-05', '2024-02-05'),
(3, 3, 'Physics Exam Preparation', 'Comprehensive study for mid-term exam', 'monthly', 'Physics', 'hard', 'active', 40, '2024-01-10', '2024-02-10'),
(4, 4, 'Chinese Literature Analysis', 'Analysis of classical Chinese poems', 'weekly', 'Chinese', 'medium', 'completed', 100, '2023-12-01', '2024-01-15'),
(5, 5, 'Chemistry Lab Experiments', 'Preparation for lab experiments', 'semester', 'Chemistry', 'hard', 'active', 15, '2024-01-02', '2024-06-30'),
(6, 6, 'Biology Study Guide', 'Study guide for biology final exam', 'monthly', 'Biology', 'medium', 'paused', 30, '2024-01-08', '2024-02-08'),
(7, 7, 'History Timeline Project', 'Create timeline of major historical events', 'weekly', 'History', 'easy', 'active', 75, '2024-01-12', '2024-01-26'),
(8, 8, 'Geography Map Quiz', 'Prepare for map quiz on world capitals', 'daily', 'Geography', 'easy', 'completed', 100, '2024-01-03', '2024-01-17'),
(9, 9, 'Computer Science Programming', 'Learn Java programming basics', 'semester', 'Computer Science', 'medium', 'active', 20, '2024-01-01', '2024-06-30'),
(10, 10, 'Mathematics Problem Solving', 'Daily practice of math problems', 'daily', 'Mathematics', 'hard', 'active', 50, '2024-01-06', '2024-02-06');