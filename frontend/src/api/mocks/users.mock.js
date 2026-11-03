export const mockProfile = {
  id: 2,
  username: 'hicham10',
  avatarUrl: '/uploads/avatars/user_2.jpg',
  bio: 'Ticket trader based in Chengdu. Mostly resell football and concert tickets.',
  verificationStatus: 'fully_verified',
  trustScore: {
    score: 42,
    totalAuctions: 15,
    successfulAuctions: 14,
    disputes: 1
  },
  reviews: [
    { reviewerUsername: 'orgil99', starRating: 5, comment: 'Fast and reliable seller.', createTime: '2026-06-20T10:30:00' },
    { reviewerUsername: 'noura38', starRating: 4, comment: 'Ticket was as described, slight delay in delivery.', createTime: '2026-06-15T09:12:00' },
    { reviewerUsername: 'otgon08', starRating: 5, comment: 'Smooth transaction, would buy again.', createTime: '2026-06-10T16:45:00' }
  ]
}