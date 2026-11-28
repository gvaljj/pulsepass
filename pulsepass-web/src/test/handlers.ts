import { http, HttpResponse } from 'msw'

const venue = {
  id: 1,
  code: 'TEATRO-MAIN',
  name: 'Teatro Principal',
  city: 'Cartagena',
  address: 'Centro Histórico',
  capacity: 500,
  active: true,
}

const event = {
  id: 1,
  eventCode: 'JAZZ-2026',
  name: 'Noche de Jazz y Cultura',
  description: 'Tres noches de música caribeña frente a la bahía.',
  category: 'MUSIC',
  status: 'PUBLISHED',
  eventDate: '2026-12-12T20:00:00',
  minimumAge: 18,
  venueCode: 'TEATRO-MAIN',
  venueName: 'Teatro Principal',
  artists: [{ id: 1, stageName: 'Solar Beat', genre: 'Electronica', active: true }],
}

const ticket = {
  id: 1,
  ticketCode: 'TKT-001',
  type: 'VIP',
  price: 250000,
  status: 'PAID',
  purchaseDate: '2026-11-22T10:00:00',
  userEmail: 'andrea@pulsepass.com',
  eventCode: 'JAZZ-2026',
  eventName: 'Noche de Jazz y Cultura',
}

export const handlers = [
  http.get('/api/events/published', () => HttpResponse.json([event])),

  http.get('/api/events/JAZZ-2026', () => HttpResponse.json(event)),

  http.get('/api/events/by-artist', ({ request }) => {
    const url = new URL(request.url)
    if (url.searchParams.get('stageName') === 'Solar Beat') {
      return HttpResponse.json([event])
    }
    return HttpResponse.json([])
  }),

  http.get('/api/artists/active', () =>
    HttpResponse.json([
      { id: 1, stageName: 'Solar Beat', genre: 'Electronica', active: true },
      { id: 2, stageName: 'Neon Waves', genre: 'Synthwave', active: true },
    ]),
  ),

  http.get('/api/venues/active', () => HttpResponse.json([venue])),

  http.get('/api/venues/TEATRO-MAIN', () => HttpResponse.json(venue)),

  http.post('/api/auth/login', () =>
    HttpResponse.json({
      accessToken: 'token-de-prueba',
      tokenType: 'Bearer',
      expiresIn: 3600,
    }),
  ),

  http.get('/api/auth/me', ({ request }) => {
    const auth = request.headers.get('Authorization')
    if (auth?.includes('admin')) {
      return HttpResponse.json({
        id: 2,
        username: 'admin',
        email: 'admin@pulsepass.com',
        roles: ['ADMIN'],
        active: true,
      })
    }
    return HttpResponse.json({
      id: 1,
      username: 'andrea',
      email: 'andrea@pulsepass.com',
      roles: ['USER'],
      active: true,
    })
  }),

  http.post('/api/users', async ({ request }) => {
    const body = (await request.json()) as { username: string; email: string }
    return HttpResponse.json(
      {
        id: 3,
        username: body.username,
        email: body.email,
        firstName: 'Test',
        lastName: 'User',
        birthDate: '1995-05-10',
        active: true,
      },
      { status: 201 },
    )
  }),

  http.get('/api/users/by-email', ({ request }) => {
    const url = new URL(request.url)
    return HttpResponse.json({
      id: 1,
      username: 'andrea',
      email: url.searchParams.get('email') ?? 'andrea@pulsepass.com',
      firstName: 'Andrea',
      lastName: 'Ruiz',
      birthDate: '1995-05-10',
      active: true,
    })
  }),

  http.post('/api/tickets', async ({ request }) => {
    const body = (await request.json()) as { userEmail: string; eventCode: string; type: string }
    return HttpResponse.json(
      {
        ...ticket,
        userEmail: body.userEmail,
        eventCode: body.eventCode,
        type: body.type,
      },
      { status: 201 },
    )
  }),

  http.get('/api/tickets/TKT-001', () => HttpResponse.json(ticket)),

  http.get('/api/tickets/by-user', ({ request }) => {
    const url = new URL(request.url)
    if (url.searchParams.get('email') === 'andrea@pulsepass.com') {
      return HttpResponse.json([ticket])
    }
    return HttpResponse.json([])
  }),

  http.patch('/api/tickets/TKT-001/cancel', () =>
    HttpResponse.json({ ...ticket, status: 'CANCELLED' }),
  ),

  http.patch('/api/tickets/TKT-001/use', () =>
    HttpResponse.json({ ...ticket, status: 'USED' }),
  ),

  http.get('/api/events/JAZZ-2026/tickets/paid', () => HttpResponse.json([ticket])),

  http.post('/api/events', async ({ request }) => {
    const body = (await request.json()) as { eventCode: string; name: string }
    return HttpResponse.json(
      {
        ...event,
        eventCode: body.eventCode,
        name: body.name,
        status: 'DRAFT',
      },
      { status: 201 },
    )
  }),

  http.patch('/api/events/JAZZ-2026/publish', () =>
    HttpResponse.json({ ...event, status: 'PUBLISHED' }),
  ),

  http.post('/api/events/JAZZ-2026/artists/1', () =>
    HttpResponse.json({ ...event, artists: event.artists }),
  ),
]