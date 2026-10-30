# Deterministic, verbatim copy of religious text from source datasets into app assets. No text is written by hand.
import json, hashlib, sys
A='/workspace/apps/prayer-times/app/src/main/assets/'
src=json.load(open('/workspace/data/HisnElMuslim/hisn.json'))
icons={'الصباح':'morning','المساء':'evening','النوم':'sleep','الاستيقاظ':'wake','الاسْتِيقَاظ':'wake','الـمَسْجِد':'mosque','المسجد':'mosque','الصَّلَاة':'prayer','الرُّكُوع':'prayer','السُّجُود':'prayer','التَّشَهُّد':'prayer','الوُضُوء':'water','الخَلَاء':'water','السَّفَر':'travel','الرُّكُوب':'travel','الطَّعَام':'food','الصَّائِم':'food','الاسْتِغْفَار':'repent','المَطَر':'rain','الرِّيح':'rain','المَرِيض':'health','المَيِّت':'grave','القُبُور':'grave','الثَّوْب':'cloth','المَنْزِل':'home','الأَذَان':'adhan','الحَجّ':'hajj','الصَّفَا':'hajj','عَرَفَة':'hajj','الجِمَار':'hajj'}
def strip(s):
    import unicodedata; return ''.join(c for c in s if not ('\u064b'<=c<='\u0652' or c in '\u0670\u0640'))
def icon(t):
    st=strip(t)
    for k,v in icons.items():
        if strip(k) in st: return v
    return 'dua'
cats=[]
for i,(title,v) in enumerate(src.items()):
    items=[{'id':f'h{i}_{j}','text':a['Text'],'count':a['Count'],'ref':a['Reference']} for j,a in enumerate(v['Adhkar'])]
    cats.append({'id':f'h{i}','title':title,'icon':icon(title),'items':items})
# Owner-approved corrections (docs/text-corrections.json), applied verbatim and logged.
import os
_cf=os.path.join(os.path.dirname(os.path.abspath(__file__)),'..','docs','text-corrections.json')
_corr=json.load(open(_cf,encoding='utf-8'))['corrections'] if os.path.exists(_cf) else []
_byid={it['id']:it for c in cats for it in c['items']}
for c in _corr:
    it=_byid[c['item']]
    if it['text'].count(c['find'])!=1: sys.exit(f"correction for {c['item']}: 'find' must occur exactly once")
    it['text']=it['text'].replace(c['find'],c['replace']); it['corrected']=True
    print('applied correction',c['item'])
json.dump({'source':'asellam/HisnElMuslim@872764ca013116c905f7fc9c3dc972c27cd37d4a (MIT)','categories':cats},open(A+'hisn.json','w'),ensure_ascii=False,indent=0)
# Quran duas (Tanzil Uthmani, verbatim). Selection of ayah references only.
q={}
for l in open('/workspace/data/quran-uthmani.txt',encoding='utf-8'):
    if l.startswith('#') or '|' not in l: continue
    s,a,t=l.rstrip('\n').split('|',2); q[(int(s),int(a))]=t
def ay(s,a,b=None):
    b=b or a; return {'id':f'q{s}_{a}_{b}','text':' '.join(q[(s,x)]+f' ﴿{x}﴾' for x in range(a,b+1)),'ref':f'{s}:{a}' + (f'-{b}' if b!=a else ''),'surah':s,'from':a,'to':b}
QD=[(2,127,128),(2,201),(2,250),(2,286),(3,8,9),(3,16),(3,53),(3,147),(3,191,194),(7,23),(7,47),(7,126),(10,85,86),(14,40,41),(17,24),(17,80),(18,10),(20,25,28),(20,114),(21,83),(21,87),(21,89),(23,97,98),(23,109),(23,118),(25,65,66),(25,74),(27,19),(28,16),(28,24),(40,7,9),(46,15),(59,10),(60,4,5),(66,8),(71,28)]
RQ=[(1,1,7),(2,255),(2,285,286),(112,1,4),(113,1,5),(114,1,6)]
# Sunnah duas: Hisn chapter indices with duas (verbatim items)
SUN=[23,34,35,40,41,43,44,129]
RQH=[48,49,124,125]
def hisn(idx): return [dict(it,chapter=cats[i]['title']) for i in idx for it in cats[i]['items']]
notice=''.join(l for l in open('/workspace/data/quran-uthmani.txt',encoding='utf-8') if l.startswith('#'))
duas={'quran_notice':notice,'quran_source':'Tanzil Project, Quran text Uthmani 1.1, https://tanzil.net (verbatim, unmodified)','sections':[
 {'id':'quran','items':[ay(*x) for x in QD]},
 {'id':'sunnah','items':hisn(SUN)},
 {'id':'ruqyah','items':[ay(*x) for x in RQ]+hisn(RQH)}]}
json.dump(duas,open(A+'duas.json','w'),ensure_ascii=False,indent=0)
print(len(cats),sum(len(c['items']) for c in cats),[len(s['items']) for s in duas['sections']])
