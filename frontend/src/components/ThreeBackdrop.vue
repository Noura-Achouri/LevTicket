<template>
  <div ref="hostRef" class="three-backdrop" aria-hidden="true">
    <canvas ref="canvasRef" class="three-backdrop__canvas" />
  </div>
</template>

<script setup>
/**
 * Animated 3D backdrop: floating ticket stubs, a slow wireframe globe and a
 * particle field, drawn behind page content.
 *
 * - Three.js renders the scene, GSAP drives the floating/rotating motion.
 * - The canvas is transparent so the page's CSS gradient stays visible.
 * - Scene pauses when the tab is hidden and renders a single static frame when
 *   the user prefers reduced motion.
 */
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as THREE from 'three'
import gsap from 'gsap'

const hostRef = ref(null)
const canvasRef = ref(null)

const TICKET_COUNT = 13
const STAR_COUNT = 750

let renderer = null
let scene = null
let camera = null
let timer = null
let rafId = null
let resizeObserver = null
let gsapContext = null
// meshes we animate/rotate each frame
const spinning = []
// everything we must dispose
const disposables = []

const pointer = { x: 0, y: 0 }
const pointerTarget = { x: 0, y: 0 }
// false when the user asks for reduced motion: one static frame, no looping
let motionEnabled = false

function prefersReducedMotion() {
  return typeof window.matchMedia === 'function'
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.arcTo(x + w, y, x + w, y + h, r)
  ctx.arcTo(x + w, y + h, x, y + h, r)
  ctx.arcTo(x, y + h, x, y, r)
  ctx.arcTo(x, y, x + w, y, r)
  ctx.closePath()
}

/** Paints a stylised ticket stub on a 2D canvas and returns it as a texture. */
function createTicketTexture() {
  const width = 512
  const height = 320
  const radius = 44
  const stubX = width - 150

  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')

  ctx.save()
  roundRect(ctx, 0, 0, width, height, radius)
  ctx.clip()

  const body = ctx.createLinearGradient(0, 0, width, height)
  body.addColorStop(0, '#1e40af')
  body.addColorStop(0.52, '#2563eb')
  body.addColorStop(1, '#38bdf8')
  ctx.fillStyle = body
  ctx.fillRect(0, 0, width, height)

  // top sheen so the plane catches the light like a laminated stub
  const sheen = ctx.createLinearGradient(0, 0, 0, height * 0.55)
  sheen.addColorStop(0, 'rgba(255, 255, 255, 0.42)')
  sheen.addColorStop(1, 'rgba(255, 255, 255, 0)')
  ctx.fillStyle = sheen
  ctx.fillRect(0, 0, width, height * 0.55)

  // darker stub half
  ctx.fillStyle = 'rgba(2, 6, 23, 0.22)'
  ctx.fillRect(stubX, 0, width - stubX, height)

  // tear-off perforation
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.72)'
  ctx.lineWidth = 4
  ctx.setLineDash([13, 15])
  ctx.beginPath()
  ctx.moveTo(stubX, 0)
  ctx.lineTo(stubX, height)
  ctx.stroke()
  ctx.setLineDash([])

  // QR-ish block on the stub
  ctx.fillStyle = 'rgba(255, 255, 255, 0.85)'
  roundRect(ctx, stubX + 34, height / 2 - 44, 80, 80, 14)
  ctx.fill()

  // barcode lines
  ctx.fillStyle = 'rgba(255, 255, 255, 0.55)'
  let cursor = 46
  for (let i = 0; i < 11; i += 1) {
    const barWidth = i % 3 === 0 ? 7 : 4
    ctx.fillRect(cursor, 74, barWidth, 78)
    cursor += barWidth + 9
  }

  ctx.restore()

  // crisp rounded outline
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.7)'
  ctx.lineWidth = 5
  roundRect(ctx, 3, 3, width - 6, height - 6, radius - 3)
  ctx.stroke()

  const texture = new THREE.CanvasTexture(canvas)
  texture.colorSpace = THREE.SRGBColorSpace
  texture.anisotropy = 4
  disposables.push(texture)
  return texture
}

function buildTickets() {
  const texture = createTicketTexture()
  const geometry = new THREE.PlaneGeometry(3.2, 2)
  disposables.push(geometry)

  const material = new THREE.MeshStandardMaterial({
    map: texture,
    transparent: true,
    roughness: 0.4,
    metalness: 0.2,
    side: THREE.DoubleSide,
    depthWrite: false
  })
  disposables.push(material)

  for (let i = 0; i < TICKET_COUNT; i += 1) {
    const ticket = new THREE.Mesh(geometry, material)
    // spread across a shallow depth band so the fog separates the planes
    const depth = -16 + Math.random() * 19
    // keep out of the centre column, where the white form card covers them
    const side = i % 2 === 0 ? -1 : 1
    ticket.position.set(
      side * (6.5 + Math.random() * 9.5),
      (Math.random() - 0.5) * 20,
      depth
    )
    ticket.rotation.set(
      (Math.random() - 0.5) * 0.5,
      (Math.random() - 0.5) * 0.9,
      (Math.random() - 0.5) * 0.6
    )
    // distant planes fade back instead of dominating the frame
    const scale = THREE.MathUtils.mapLinear(depth, -16, 3, 0.55, 1.25)
    ticket.scale.setScalar(scale * (0.85 + Math.random() * 0.4))
    scene.add(ticket)
    spinning.push(ticket)
  }
}

function buildGlobe() {
  const geometry = new THREE.IcosahedronGeometry(9, 1)
  disposables.push(geometry)

  const material = new THREE.MeshBasicMaterial({
    color: 0x60a5fa,
    wireframe: true,
    transparent: true,
    opacity: 0.16,
    depthWrite: false
  })
  disposables.push(material)

  const globe = new THREE.Mesh(geometry, material)
  globe.position.set(0, 0, -9)
  scene.add(globe)
  spinning.push(globe)
  return globe
}

function buildStars() {
  const positions = new Float32Array(STAR_COUNT * 3)
  for (let i = 0; i < STAR_COUNT; i += 1) {
    positions[i * 3] = (Math.random() - 0.5) * 52
    positions[i * 3 + 1] = (Math.random() - 0.5) * 34
    positions[i * 3 + 2] = (Math.random() - 0.5) * 38 - 8
  }

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  disposables.push(geometry)

  const material = new THREE.PointsMaterial({
    color: 0xa5b4fc,
    size: 0.08,
    sizeAttenuation: true,
    transparent: true,
    opacity: 0.8,
    depthWrite: false,
    blending: THREE.AdditiveBlending
  })
  disposables.push(material)

  const stars = new THREE.Points(geometry, material)
  scene.add(stars)
  return stars
}

function sizeToHost() {
  const host = hostRef.value
  if (!host || !renderer || !camera) return
  const width = host.clientWidth || 1
  const height = host.clientHeight || 1
  renderer.setSize(width, height, false)
  camera.aspect = width / height
  camera.updateProjectionMatrix()
}

function renderFrame() {
  timer.update()
  // clamp delta so a stalled tab doesn't snap the scene forward
  const delta = Math.min(timer.getDelta(), 0.05)
  const elapsed = timer.getElapsed()

  // ease the camera toward the pointer for a parallax feel
  pointer.x += (pointerTarget.x - pointer.x) * 0.05
  pointer.y += (pointerTarget.y - pointer.y) * 0.05
  camera.position.x = pointer.x * 2.2
  camera.position.y = -pointer.y * 1.5
  camera.lookAt(0, 0, 0)

  spinning.forEach((mesh, index) => {
    if (mesh.isPoints) {
      // starfield turns almost imperceptibly
      mesh.rotation.y = elapsed * 0.008
      return
    }
    if (mesh.geometry.type === 'IcosahedronGeometry') {
      mesh.rotation.y = elapsed * 0.05
      mesh.rotation.x = elapsed * 0.022
      return
    }
    // gentle roll, in radians per second so it is refresh-rate independent
    mesh.rotation.z += (0.05 + (index % 5) * 0.02) * delta
  })

  renderer.render(scene, camera)
}

function loop() {
  renderFrame()
  rafId = requestAnimationFrame(loop)
}

function startLoop() {
  if (rafId === null) rafId = requestAnimationFrame(loop)
}

function stopLoop() {
  if (rafId !== null) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
}

function handlePointerMove(event) {
  pointerTarget.x = (event.clientX / window.innerWidth) * 2 - 1
  pointerTarget.y = (event.clientY / window.innerHeight) * 2 - 1
}

function handleVisibilityChange() {
  if (!motionEnabled) return
  if (document.hidden) stopLoop()
  else startLoop()
}

function init() {
  const canvas = canvasRef.value
  const host = hostRef.value
  if (!canvas || !host) return

  try {
    renderer = new THREE.WebGLRenderer({
      canvas,
      antialias: true,
      alpha: true,
      powerPreference: 'high-performance'
    })
  } catch (error) {
    // no WebGL: leave the CSS gradient as the background
    renderer = null
    return
  }

  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.setClearAlpha(0)

  scene = new THREE.Scene()
  // fog colour matches the page gradient so distant objects melt into it
  scene.fog = new THREE.FogExp2(0x0a0f26, 0.03)

  camera = new THREE.PerspectiveCamera(54, 1, 0.1, 120)
  camera.position.z = 15
  timer = new THREE.Timer()
  // timers connected to the document skip the jump after a hidden tab
  timer.connect?.(document)

  scene.add(new THREE.AmbientLight(0x8ea6d8, 1.15))

  const keyLight = new THREE.DirectionalLight(0xffffff, 1.3)
  keyLight.position.set(-6, 8, 9)
  scene.add(keyLight)

  const blueLight = new THREE.PointLight(0x38bdf8, 90, 46)
  blueLight.position.set(9, 6, 6)
  scene.add(blueLight)

  const violetLight = new THREE.PointLight(0x8b5cf6, 80, 46)
  violetLight.position.set(-10, -7, 4)
  scene.add(violetLight)

  buildTickets()
  buildGlobe()
  const stars = buildStars()
  spinning.push(stars)

  sizeToHost()

  window.addEventListener('pointermove', handlePointerMove, { passive: true })
  document.addEventListener('visibilitychange', handleVisibilityChange)

  resizeObserver = new ResizeObserver(sizeToHost)
  resizeObserver.observe(host)

  if (prefersReducedMotion()) {
    // one static frame: no drift, no spin, no fade
    motionEnabled = false
    renderFrame()
    return
  }

  motionEnabled = true

  gsapContext = gsap.context(() => {
    // whole-camera breathing for a slow, cinematic pulse
    gsap.to(camera.position, {
      z: 13.4,
      duration: 9,
      yoyo: true,
      repeat: -1,
      ease: 'sine.inOut'
    })

    // each ticket drifts, spins and rises independently
    spinning.forEach((mesh, index) => {
      if (mesh.isPoints) return
      if (mesh.geometry.type === 'IcosahedronGeometry') return

      gsap.to(mesh.position, {
        y: `+=${1.1 + Math.random() * 1.9}`,
        x: `+=${(Math.random() - 0.5) * 1.4}`,
        duration: 3.6 + Math.random() * 3.4,
        yoyo: true,
        repeat: -1,
        ease: 'sine.inOut',
        delay: index * 0.17
      })

      gsap.to(mesh.rotation, {
        y: mesh.rotation.y + Math.PI * (Math.random() < 0.5 ? 2 : -2),
        duration: 24 + Math.random() * 22,
        repeat: -1,
        ease: 'none'
      })

      gsap.to(mesh.rotation, {
        x: mesh.rotation.x + (Math.random() - 0.5) * 0.9,
        yoyo: true,
        repeat: -1,
        duration: 7 + Math.random() * 5,
        ease: 'sine.inOut'
      })
    })

    gsap.fromTo(host, { opacity: 0 }, { opacity: 1, duration: 1.6, ease: 'power2.out' })
  })

  startLoop()
}

function teardown() {
  motionEnabled = false
  stopLoop()
  window.removeEventListener('pointermove', handlePointerMove)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (gsapContext) {
    gsapContext.revert()
    gsapContext = null
  }
  if (timer) {
    timer.disconnect?.()
    timer.dispose?.()
    timer = null
  }
  disposables.forEach((item) => item.dispose())
  disposables.length = 0
  spinning.length = 0
  if (renderer) {
    renderer.dispose()
    renderer.forceContextLoss?.()
    renderer = null
  }
  scene = null
  camera = null
}

onMounted(init)
onBeforeUnmount(teardown)
</script>

<style scoped>
/* Sits behind page content; the parent supplies the positioning context. */
.three-backdrop {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.three-backdrop__canvas {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
