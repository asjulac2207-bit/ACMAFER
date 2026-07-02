/* ACMAFER — Particles Background (micro-particles dissolving/fading) */
(function(){
  const c=document.getElementById('particlesBg');
  if(!c)return;
  const ctx=c.getContext('2d');
  let W,H,pts=[],animId;
  const COLS=['rgba(255,106,0,','rgba(230,92,0,','rgba(204,112,50,','rgba(255,138,51,','rgba(37,99,235,','rgba(255,255,102,','rgba(156,163,175,'];
  function resize(){W=c.width=window.innerWidth;H=c.height=window.innerHeight}
  class Pt{
    constructor(){this.init()}
    init(){
      this.x=Math.random()*W;this.y=Math.random()*H;
      this.sz=Math.random()*2.2+.4;this.bsz=this.sz;
      this.col=COLS[Math.floor(Math.random()*COLS.length)];
      this.spd=Math.random()*.35+.1;
      this.vx=(Math.random()-.5)*.25;this.vy=-this.spd;
      this.life=Math.random();this.decay=Math.random()*.012+.003;
      this.phase=Math.random()*Math.PI*2;this.wob=Math.random()*.35+.1;
      this.op=Math.random()*.5+.15;this.melt=Math.random()*.0018+.0004;
    }
    update(){
      this.life-=this.decay;this.phase+=.02;
      this.x+=Math.sin(this.phase)*this.wob+this.vx;this.y+=this.vy;
      this.sz-=this.melt;this.op-=this.decay*.5;
      if(Math.random()<.004)this.op+=Math.random()*.25;
      if(this.life<=0||this.sz<=0||this.op<=0||this.y<-20){
        this.init();this.y=H+10;this.op=Math.random()*.3+.08;this.sz=this.bsz;this.life=1;
      }
    }
    draw(){
      const a=Math.max(0,Math.min(this.op,1));
      const g=ctx.createRadialGradient(this.x,this.y,0,this.x,this.y,this.sz*3);
      g.addColorStop(0,this.col+a+')');
      g.addColorStop(.4,this.col+(a*.4)+')');
      g.addColorStop(1,this.col+'0)');
      ctx.beginPath();ctx.arc(this.x,this.y,this.sz*3,0,Math.PI*2);
      ctx.fillStyle=g;ctx.fill();
      ctx.beginPath();ctx.arc(this.x,this.y,this.sz*.5,0,Math.PI*2);
      ctx.fillStyle=this.col+Math.min(1,a*1.4)+')';ctx.fill();
    }
  }
  function init(){
    const n=Math.min(Math.floor(W*H/8500),140);
    pts=[];for(let i=0;i<n;i++){const p=new Pt();p.y=Math.random()*H;p.life=Math.random();pts.push(p)}
  }
  function loop(){
    ctx.clearRect(0,0,W,H);
    for(let i=0;i<pts.length;i++){
      pts[i].update();pts[i].draw();
      for(let j=i+1;j<pts.length;j++){
        const dx=pts[i].x-pts[j].x,dy=pts[i].y-pts[j].y,d=Math.sqrt(dx*dx+dy*dy);
        if(d<75){
          ctx.beginPath();ctx.moveTo(pts[i].x,pts[i].y);ctx.lineTo(pts[j].x,pts[j].y);
          ctx.strokeStyle='rgba(255,106,0,'+(1-d/75)*.035+')';
          ctx.lineWidth=.5;ctx.stroke();
        }
      }
    }
    animId=requestAnimationFrame(loop);
  }
  function start(){resize();init();if(animId)cancelAnimationFrame(animId);loop()}
  window.addEventListener('resize',()=>{resize();init()});
  start();
})();
