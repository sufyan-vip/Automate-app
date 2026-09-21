import csv, collections, re

rows = list(csv.DictReader(open(__import__('os').path.join(__import__('os').path.dirname(__file__),'budget-model.csv'))))
secs = collections.OrderedDict()
for r in rows:
    secs.setdefault(r['Section'], []).append(r)

def is_total(item):
    return bool(re.search(r'(SUBTOTAL|TOTAL)', item.upper()))

ok = True
for sec, rs in secs.items():
    if sec in ('COWORKING_SURPLUS','PREREQ_SAVINGS') or sec.startswith('REVENUE_'):
        # cross-check specific claims instead
        continue
    items = [r for r in rs if not is_total(r['Item'])]
    totals = [r for r in rs if is_total(r['Item'])]
    if not totals:
        continue
    for t in totals:
        # tier A capex has a without-solar subtotal and a with-solar grand total
        if sec == 'TIER_A_CAPEX':
            if 'without' in t['Item']:
                s = sum(int(r['Total_PKR']) for r in items if 'Solar' not in r['Item'])
            else:
                s = sum(int(r['Total_PKR']) for r in items)
        elif sec == 'TIER_A_MONTHLY':
            if 'without' in t['Item']:
                s = sum(int(r['Total_PKR']) for r in items if 'WITH 6kW' not in r['Item'] and 'Solar O and M' not in r['Item'] and 'Solar 6kW' not in r['Item'])
            else:
                s = sum(int(r['Total_PKR']) for r in items if 'WITHOUT solar' not in r['Item'])
        else:
            s = sum(int(r['Total_PKR']) for r in items)
        stated = int(t['Total_PKR'])
        flag = 'OK ' if s == stated else 'MISMATCH'
        if s != stated: ok = False
        print(f"{flag} {sec:26s} {t['Item']:34s} computed={s:>10,} stated={stated:>10,} delta={stated-s:>+9,}")

print()
# line-item integrity: Qty*Unit == Total where both present
print("--- line item Qty x Unit check ---")
bad = 0
for r in rows:
    if r['Qty'] and r['Unit_PKR'] and r['Total_PKR'] and not is_total(r['Item']):
        q,u,t = int(r['Qty']), int(r['Unit_PKR']), int(r['Total_PKR'])
        if q*u != t:
            bad += 1; ok = False
            print(f"  BAD {r['Section']:22s} {r['Item'][:45]:47s} {q} x {u} = {q*u} != {t}")
print(f"  {bad} inconsistencies")

print()
print("--- stated-claim checks from the roadmap doc ---")
def num(section, needle):
    for r in rows:
        if r['Section']==section and needle.lower() in r['Item'].lower():
            return int(r['Total_PKR'])

FX = 278
checks = []
ta = num('TIER_A_MONTHLY','TOTAL_without_solar')
tas = num('TIER_A_MONTHLY','TOTAL_with_solar')
tb = num('TIER_B_MONTHLY','GROSS_TOTAL')
cw_rev = num('COWORKING_REVENUE','TOTAL')
cw_cost = num('COWORKING_COST','TOTAL')
cwl = num('COWORKING_SETUP_LEAN','TOTAL')
cwf = num('COWORKING_SETUP_FULL','TOTAL')
checks.append(("Tier A capex (no solar) = 16,62,000", num('TIER_A_CAPEX','SUBTOTAL_without_solar')==1662000))
checks.append(("Tier A capex with solar = 27,62,000", num('TIER_A_CAPEX','GRAND_TOTAL_with_solar')==2762000))
checks.append(("Tier B capex = 59,57,000", num('TIER_B_CAPEX','GRAND_TOTAL')==5957000))
checks.append(("Tier A burn/mo = 5,36,000", ta==536000))
checks.append(("Tier A burn with solar = 5,20,000", tas==520000))
checks.append(("Tier B gross burn = 12,56,000", tb==1256000))
checks.append(("Co-working revenue = 1,49,600", cw_rev==149600))
checks.append(("Co-working cost = 1,09,000", cw_cost==109000))
checks.append(("Co-working surplus = 40,600", cw_rev-cw_cost==40600))
checks.append(("Co-working lean setup = 6,11,000", cwl==611000))
checks.append(("Co-working full setup = 13,64,000", cwf==1364000))
checks.append(("6-mo runway Tier A = 32,16,000 (~32 lakh)", 6*ta==3216000))
checks.append(("Grand total savings w/ solar = 65,78,000", 1662000+1100000+6*ta+600000==6578000))
checks.append(("Grand total savings w/o solar = 54,78,000", 1662000+6*ta+600000==5478000))
checks.append(("Tier A burn in USD = ~$1,930", abs(round(ta/FX)-1928)<=5))
checks.append(("Tier B net burn (after coworking) = 11,06,400", tb-cw_rev==1106400))
checks.append(("Tier B net burn USD = ~$3,980", abs(round((tb-cw_rev)/FX)-3980)<=10))
checks.append(("Co-working breakeven = 5 desks (4.73 computed)", round((cw_cost-64100)/9500,2)==4.73))
checks.append(("12 desks + cabin full occupancy = 1,78,100", 12*9500+64100==178100))
for name,res in checks:
    print(("PASS " if res else "FAIL ")+name)
    if not res: ok=False

print()
print("--- MRR projection cross-check ---")
for m,(intl,local,proj,tot) in {3:(600000,50000,200000,850000),6:(1000000,108000,300000,1408000),
                                 12:(1600000,280000,400000,2280000),36:(4500000,1125000,800000,6425000)}.items():
    s=intl+local+proj
    print(("PASS " if s==tot else "FAIL ")+f"M{m}: {intl:,} + {local:,} + {proj:,} = {s:,} (doc says {tot:,})")
    if s!=tot: ok=False

print()
print("--- local MRR table check (clients x AMC) ---")
for mo,cl,amc,tot in [(6,6,18000,108000),(12,14,20000,280000),(24,28,22000,616000),(36,45,25000,1125000)]:
    v=cl*amc
    print(("PASS " if v==tot else "FAIL ")+f"M{mo}: {cl} clients x Rs {amc:,} = {v:,} (doc says {tot:,})")
    if v!=tot: ok=False

print()
print("ALL CHECKS PASSED" if ok else "SOME CHECKS FAILED")
