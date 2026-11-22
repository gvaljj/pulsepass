import { http, HttpResponse } from 'msw'

export const handlers = [
  http.get('/api/events/published', () =>
    HttpResponse.json([
      {
        id: 1,
        eventCode: 'JAZZ-2026',
        name: 'Noche de Jazz',
        category: 'MUSIC',
        status: 'PUBLISHED',
        eventDate: '2026-12-12T20:00:00',
        venueCode: 'TEATRO-MAIN',
      },
    ]),
  ),

  http.get('/api/artists/active', () =>
    HttpResponse.json([
      {
        id: 1,
        stageName: 'Solar Beat',
        genre: 'Jazz',
        active: true,
      },
    ]),
  ),

  http.get('/api/venues/active', () =>
    HttpResponse.json([
      {
        id: 1,
        code: 'TEATRO-MAIN',
        name: 'Teatro Principal',
        city: 'Cartagena',
        address: 'Centro',
        capacity: 500,
        active: true,
      },
    ]),
  ),

  http.post('/api/auth/login', () =>
    HttpResponse.json({
      accessToken: 'token-de-prueba',
      tokenType: 'Bearer',
      expiresIn: 3600,
    }),
  ),

  http.get('/api/auth/me', () =>
    HttpResponse.json({
      id: 1,
      username: 'andrea',
      email: 'andrea@pulsepass.com',
      roles: ['USER'],
      active: true,
    }),
  ),
]