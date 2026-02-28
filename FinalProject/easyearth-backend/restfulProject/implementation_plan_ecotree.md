# Implementation Plan - Eco Tree Growth Game

The Eco Tree game allows users to grow a virtual tree using points earned through attendance, quizzes, and quests.

## 1. Backend Tasks

### 1.1 Database Setup
- [ ] Execute `eco_tree.sql` to create the `ECO_TREE` table and sequence.
- [ ] Initialize `ECO_TREE` record when a user first accesses the game.

### 1.2 Model & Data Access
- [ ] Create `com.kh.spring.ecotree.model.vo.EcoTreeVO`.
- [ ] Create `com.kh.spring.ecotree.mapper.EcoTreeMapper` interface.
- [ ] Create `resources/mappers/ecotree-mapper.xml`.
  - `selectTreeByMemberId`
  - `insertTree`
  - `updateTreeExp`
  - `getPointBalance`

### 1.3 Service Layer
- [ ] Create `com.kh.spring.ecotree.service.EcoTreeService`.
  - Logic to handle "watering" (spending 500 points for 10 EXP).
  - Logic for leveling up (Level 1: 0-100, Level 2: 101-300, Level 3: 301-600, Level 4: Max).

### 1.4 API Layer
- [ ] Create `com.kh.spring.ecotree.controller.EcoTreeController`.
  - `GET /ecotree/{memberId}` (Swagger: 나무 상태 조회)
  - `POST /ecotree/grow` (Swagger: 나무 성장 시키기 - 포인트 차감)

## 2. Frontend Tasks

### 2.1 API Integration
- [ ] Create `src/apis/ecotreeApi.js`.

### 2.2 Components
- [ ] Create `src/components/main/EcoTreeModal.jsx`.
  - Display tree image based on level.
  - Progress bar for EXP.
  - "Water Tree" button.
  - Points display.
- [ ] Update `src/pages/MainPage/MainPage.jsx` sidebar.

### 2.3 Assets
- [ ] Generate 4 stages of tree images (Seedling, Small Tree, Big Tree, Fruit Tree).

## 3. Swagger Integration
- [ ] Add `@Tag` and `@Operation` to the new controller for documentation.
