# LevTicket Frontend
## A Vue-Based Ticket Marketplace Interface for a University Final Project

**Student:** [Add student name]  
**Student ID:** [Add student ID]  
**University:** [Add university]  
**Supervisor:** [Add supervisor]  
**Date:** [Add submission date]

---

## Abstract

LevTicket is a web-based ticket marketplace designed to help users discover, buy, sell, and bid on tickets for concerts, sports events, festivals, theatre, and other live experiences. This document presents the frontend contribution to the project. The frontend is implemented as a single-page application using Vue 3, Vite, Vue Router, Pinia, Axios, and Element Plus.

The frontend provides the visual interface and interaction layer between users and the backend services. It includes account access, ticket search and filtering, ticket publishing, auction details, bidding, order history, seller listings, profiles, reviews, and AI-supported customer assistance. The application uses reusable API modules, centralized authentication state, lazy-loaded routes, responsive layouts, and a mock-data option that supports demonstrations when the backend is unavailable.

The main result is a responsive and modular marketplace interface that turns the project's business requirements into user-facing workflows. The implementation also establishes a clear boundary between presentation logic, state management, HTTP communication, and backend responsibilities.

**Keywords:** Vue 3, Vite, ticket marketplace, single-page application, auction interface, Pinia, Axios, responsive web design, AI customer support.

---

## 1. Introduction

### 1.1 Background

Buying and selling event tickets through informal channels can be inconvenient and risky. Users may have difficulty finding suitable events, comparing listings, evaluating sellers, placing bids, or receiving support. A specialized marketplace can centralize these actions in one interface and make the process easier to understand.

LevTicket was developed as a university final project to model this type of marketplace. The system combines ticket discovery, seller and buyer workflows, auctions, orders, trust information, reviews, and AI-assisted support. The frontend is responsible for presenting these capabilities in a coherent and accessible web application.

### 1.2 Problem statement

The project addresses the following problems:

- Event tickets are difficult to search and compare when information is spread across different channels.
- Sellers need a structured form for publishing ticket information and images.
- Buyers need to inspect auction data, current bids, seller trust, and bid history before acting.
- Users need a single place to review orders, listings, profiles, and reviews.
- Users may need immediate guidance while using a marketplace with several workflows.

### 1.3 Project objectives

The frontend objectives were to:

1. Build a clear marketplace home page for discovering active tickets.
2. Support registration and login through the backend authentication API.
3. Provide searchable, filterable, and sortable ticket listings.
4. Present ticket and auction details in a form suitable for placing bids.
5. Allow authenticated sellers to create ticket listings with event data and images.
6. Display personal listings, orders, profiles, trust scores, and reviews.
7. Provide AI customer support through a chat drawer and a dedicated AI page.
8. Maintain a responsive interface for desktop, tablet, and mobile screens.
9. Keep frontend code modular so API services and views can evolve independently.

### 1.4 Scope of this document

This document focuses on the frontend implementation. The backend, database, authentication validation, auction rules, order processing, file storage, and AI response generation are backend responsibilities. The frontend connects to those capabilities through HTTP requests and presents their results to users.

---

## 2. System Overview

LevTicket is organized as a client-server application:

- **Frontend:** Vue 3 single-page application running in the browser.
- **Backend:** Spring Boot service running on `http://localhost:8080`.
- **Database and business services:** Managed by the backend and outside the frontend's direct responsibility.
- **HTTP boundary:** Axios requests are sent to backend routes under `/api`.
- **Optional demonstration mode:** Selected API modules can return local mock data when `VITE_USE_MOCKS=true`.

The browser loads the application once. Vue Router then changes the visible view without requiring a full page reload. Each route is lazy-loaded, which keeps the initial application bundle focused on the first screen and loads other views when needed.

---

## 3. Technologies and Tools

| Technology | Role in the frontend |
| --- | --- |
| Vue 3 | Component-based user interface framework |
| Vite | Development server and production build tool |
| Vue Router 4 | Client-side routing and navigation |
| Pinia | Central authentication state management |
| Axios | HTTP client for backend communication |
| Element Plus | Forms, tables, cards, dialogs, messages, upload controls, and other UI components |
| `@element-plus/icons-vue` | Icons used in interface controls |
| JavaScript ES modules | Application source language |
| CSS | Responsive layout, visual design, tokens, and component styling |

The main scripts are:

```text
npm run dev       Start the Vite development server
npm run build     Build the production bundle
npm run preview   Preview the production bundle locally
```

---

## 4. Frontend Architecture

### 4.1 Application bootstrap

The entry point is `frontend/src/main.js`. It creates the Vue application, registers Pinia, Vue Router, and Element Plus, imports global styles, and mounts the application to the `#app` element in `index.html`.

### 4.2 Root application shell

`frontend/src/App.vue` provides the persistent shell around route content. It contains:

- The LevTicket brand link.
- Main navigation links for Home, Browse, Sell, Listings, and Orders.
- Login and registration controls for anonymous users.
- Profile and logout controls for authenticated users.
- The current route rendered through `<router-view />`.
- The persistent customer-service chat button and drawer.

The navigation changes according to the presence of an authentication token in the Pinia store.

### 4.3 Routing

Routes are defined in `frontend/src/router/index.js` using `createWebHistory()`. View components are loaded with dynamic imports. The current routes are:

| Path | View | Responsibility |
| --- | --- | --- |
| `/` | `Home.vue` | Marketplace landing page and featured tickets |
| `/login` | `Login.vue` | User authentication |
| `/register` | `Register.vue` | Account registration |
| `/auction/:id` | `AuctionDetail.vue` | Ticket, auction, bid, seller, and bid history details |
| `/profile` | `Profile.vue` | Profile, trust score, and reviews |
| `/post-ticket` | `PostTicket.vue` | Create a ticket listing and auction |
| `/search` | `SearchResults.vue` | Search, filtering, sorting, and listing results |
| `/my-listings` | `MyListings.vue` | Active, sold, and expired seller listings |
| `/orders` | `OrderHistory.vue` | Bought and sold orders, including reviews |
| `/ai-chat` | `AIChat.vue` | Full-page AI assistant |

### 4.4 State management

The authentication store is defined in `frontend/src/stores/auth.js`. It stores:

- `token`: the bearer token used in authenticated requests.
- `user`: the currently known user summary after login.
- `trustScore`: a state field reserved for trust-score information.

The token is persisted in `localStorage`, allowing the browser to retain authentication state after a page refresh. `logout()` clears the token, user data, and stored token.

The application intentionally keeps most page state local to each view. For example, search filters stay in `SearchResults.vue`, bid history stays in `AuctionDetail.vue`, and order tabs stay in `OrderHistory.vue`. This keeps unrelated screens independent and avoids unnecessary global state.

### 4.5 API service layer

The `frontend/src/api/` directory isolates HTTP calls from view components. This creates a small service layer with one module per feature area:

- `auth.js`: login and registration.
- `tickets.js`: list tickets, read a ticket, create a ticket, and read the user's tickets.
- `auctions.js`: load auction data, bid history, and place bids.
- `orders.js`: load bought or sold orders and individual orders.
- `categories.js`: load ticket categories.
- `users.js`: load profiles, update profile data, upload avatars, and load trust scores.
- `reviews.js`: submit reviews.
- `ai.js`: recommendations, price insights, and AI complaints or support requests.

The shared Axios instance in `request.js` uses `http://localhost:8080/api` as its base URL, adds the bearer token when available, and unwraps the backend response envelope. The development server also proxies `/api` to the backend service.

### 4.6 Mock-data support

Mock mode is controlled by `VITE_USE_MOCKS`. The mock modules provide local examples for tickets, ticket details, auctions, orders, AI data, and users. This supports UI demonstrations when a backend is not available. The mock implementation is currently selective rather than a complete replacement for every backend operation.

### 4.7 Visual design system

Global styles are defined in `frontend/src/style.css`. The frontend uses CSS variables for brand colors, text colors, borders, shadows, spacing-related visual values, and corner radii. Element Plus is customized with the same visual language.

The interface uses:

- A dark navy and blue brand palette.
- Light, low-contrast page backgrounds.
- Card-based ticket and content surfaces.
- Clear status tags for active listings.
- Responsive grids for marketplace cards.
- Mobile breakpoints for navigation and authentication layouts.
- Loading states and empty states for data-driven screens.

---

## 5. Implemented Features

### 5.1 Marketplace home page

The home page introduces the LevTicket marketplace and displays featured ticket cards. It includes a keyword search field, event categories, marketplace metrics, trust and support promises, and navigation to the full search screen.

Ticket cards show the event name, category, current bid or starting price, status, and tags. Selecting a card opens the corresponding auction detail route.

### 5.2 Registration and login

The registration view collects username, email, password, and phone information. The login view collects email and password. After a successful login, the frontend stores the returned token and user summary in Pinia and redirects the user to the home page.

The Axios request interceptor attaches the token as:

```text
Authorization: Bearer <token>
```

Authentication validation remains a backend responsibility. The frontend handles the interaction and token lifecycle.

### 5.3 Ticket search and browsing

The search page supports:

- Keyword search by event name.
- Category filtering.
- Sorting by recent listings.
- Sorting by auctions ending soon.
- Sorting by price from low to high.
- Sorting by price from high to low.
- Empty-result feedback.
- Loading feedback while requests are running.

Search criteria are sent to the ticket API as query parameters.

### 5.4 Ticket and auction details

The auction detail view combines ticket information with auction information. It displays the event, location, date, description, seat information, seller username, seller trust score, current bid, auction end time, and bid history.

The buyer can enter a bid amount. The minimum input value is calculated as one greater than the current bid. When the bid succeeds, the displayed current bid is updated and the bid history is reloaded.

### 5.5 Ticket publishing

The seller form collects:

- Event name.
- Event date.
- Event location.
- Seat information.
- Description.
- Category.
- Comma-separated tags.
- Starting price.
- Auction duration in hours.
- Up to five images.

The form is converted to `FormData` and submitted as multipart data. After success, the user is redirected to the created ticket's auction page.

### 5.6 My listings

The listings screen separates seller tickets into Active, Sold, and Expired tabs. Each table row displays the event, current bid, status, and a link to view the listing.

### 5.7 Orders and reviews

The order screen provides Bought and Sold tabs. It displays the event, final price, status, and date. For completed orders that do not already have a review, the user can open the review modal and submit a star rating and comment.

### 5.8 Profiles, trust, and reviews

The profile screen presents the username, biography, trust score, successful-auction count, total auctions, and received reviews. Trust data helps users evaluate marketplace participants before completing transactions.

### 5.9 AI assistance

The frontend contains two AI support experiences:

1. **Customer service drawer:** available globally through the floating chat button. It supports quick questions, conversation history, loading feedback, and connection error messages.
2. **Dedicated AI chat page:** a larger assistant interface with quick questions, copy-message controls, attachment selection, audio-recording UI, camera UI, typing indicators, and a conversation history.

The implemented API call sends the conversation history and the new user message to `/api/ai/complaint`. AI response generation is performed by the backend or connected AI service.

---

## 6. User Experience and Responsive Design

The frontend was designed for repeated marketplace actions rather than a marketing-only landing page. Users can reach the primary flows from the global navigation and move between discovery, detail, selling, orders, and support without leaving the application shell.

Responsive behavior includes:

- A flexible top navigation that wraps on narrower screens.
- Reduced page padding on mobile screens.
- A single-column authentication layout below the desktop breakpoint.
- Responsive Element Plus grids for ticket cards.
- Full-width form controls on smaller screens.
- A drawer-based support experience that remains available across routes.

Loading states use Element Plus loading indicators. Empty results use `el-empty`. User feedback for successful or failed actions uses Element Plus messages.

---

## 7. Security and Reliability Considerations

The frontend contributes to security and reliability in the following ways:

- Authentication tokens are sent through the Authorization header rather than appended to URLs.
- The shared request layer centralizes authentication header behavior.
- User inputs are submitted through structured request bodies or `FormData` rather than manually assembled URLs.
- API errors are converted into user-visible messages in important flows such as login, bidding, ticket publishing, and AI support.
- Loading states prevent some duplicate actions, especially while sending AI messages.
- The frontend does not implement trust, payment, auction validation, or authorization itself; these must be enforced by the backend.

A production deployment should add HTTPS, stronger client-side validation, secure token storage strategy, route authorization guards, file type and size validation, and centralized error monitoring.

---

## 8. Testing and Verification

The frontend package defines a production build command:

```text
npm run build
```

A successful build verifies that Vite can compile the Vue application and its imports. Manual verification should cover:

- Initial page load.
- Navigation through every route.
- Login and registration with a running backend.
- Ticket search, filters, and empty results.
- Opening a ticket and reading auction data.
- Placing a bid.
- Posting a ticket and selecting images.
- Viewing listings and orders.
- Opening and submitting a review.
- Opening both AI experiences.
- Mobile and desktop layouts.
- Backend-unavailable error messages.

The frontend currently has no dedicated automated unit or end-to-end test suite in `package.json`. This is a documented limitation and a recommended extension for future work.

---

## 9. Limitations and Future Improvements

The current implementation is a strong frontend foundation, but the following items should be considered in future iterations:

1. Add route guards so unauthenticated users cannot open authenticated workflows.
2. Add form validation rules for required fields, email format, password strength, bid values, and image limits.
3. Replace the hard-coded API base URL with an environment variable for development, staging, and production deployments.
4. Complete mock coverage for every API service and operation.
5. Add automated component tests and browser-based end-to-end tests.
6. Persist or restore the authenticated user profile more completely after refresh.
7. Improve profile editing screens because the API layer already contains update and avatar functions but the visible profile page is currently read-focused.
8. Connect AI attachments, audio recording, and camera capture to backend request handling if those features are intended to be functional rather than interface demonstrations.
9. Add pagination and richer result states for large marketplaces.
10. Add accessibility testing for keyboard navigation, focus management, color contrast, and screen readers.
11. Add a centralized error boundary and notification strategy.
12. Add secure production token handling and content security policy configuration.

These items are improvement opportunities, not claims about missing backend capabilities.

---

## 10. Conclusion

The LevTicket frontend translates a ticket marketplace concept into a practical Vue single-page application. Its modular architecture separates views, reusable components, authentication state, API services, and global styling. The implemented interface supports the central buyer, seller, auction, order, review, profile, and AI-support workflows.

The project demonstrates the use of a modern frontend toolchain and shows how a client application can consume a Spring Boot backend through a consistent HTTP service layer. The mock mode, responsive design, loading states, error messages, and reusable service modules also make the frontend suitable for demonstration and continued development.

The most important next step is to strengthen automated testing and production security while preserving the current modular structure.

---

## Appendix A: Frontend File Map

```text
frontend/
  index.html
  package.json
  vite.config.js
  public/
  src/
    App.vue
    main.js
    style.css
    api/
      ai.js
      auctions.js
      auth.js
      categories.js
      config.js
      orders.js
      request.js
      reviews.js
      tickets.js
      users.js
      mocks/
    components/
      CustomerServiceChat.vue
      HelloWorld.vue
      ReviewModal.vue
      SidebarNav.vue
    router/
      index.js
    stores/
      auth.js
    views/
      AI.vue
      AIChat.vue
      AuctionDetail.vue
      Home.vue
      Login.vue
      MyListings.vue
      OrderHistory.vue
      PostTicket.vue
      Profile.vue
      Register.vue
      SearchResults.vue
      TicketList.vue
```

## Appendix B: References to the implementation

- Application bootstrap: `frontend/src/main.js`
- Root shell: `frontend/src/App.vue`
- Routes: `frontend/src/router/index.js`
- Authentication state: `frontend/src/stores/auth.js`
- HTTP client: `frontend/src/api/request.js`
- Feature APIs: `frontend/src/api/`
- Main visual system: `frontend/src/style.css`
