import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Load data
df = pd.read_csv('top3_benchmark.csv')

# Set style
plt.style.use('seaborn-v0_8')
colors = ['#2E86AB', '#A23B72', '#F18F01']

fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(15, 10))

# Runtime comparison
for i, algo in enumerate(df['Algorithm'].unique()):
    data = df[df['Algorithm'] == algo].sort_values('SupportPoint')
    ax1.plot(data['SupportPoint'], data['Runtime(ms)'] / 1000,
             marker='o', linewidth=3, markersize=8,
             color=colors[i % len(colors)], label=algo, alpha=0.8)

ax1.set_xlabel('Support Point (%)', fontweight='bold')
ax1.set_ylabel('Runtime (seconds)', fontweight='bold')
ax1.set_title('🚀 Runtime Performance', fontsize=14, fontweight='bold')
ax1.legend()
ax1.grid(True, alpha=0.3)
ax1.invert_xaxis()

# Pattern count comparison
for i, algo in enumerate(df['Algorithm'].unique()):
    data = df[df['Algorithm'] == algo].sort_values('SupportPoint')
    ax2.plot(data['SupportPoint'], data['PatternsFound'],
             marker='s', linewidth=3, markersize=8,
             color=colors[i % len(colors)], label=algo, alpha=0.8)

ax2.set_xlabel('Support Point (%)', fontweight='bold')
ax2.set_ylabel('Patterns Found', fontweight='bold')
ax2.set_title('📊 Pattern Discovery', fontsize=14, fontweight='bold')
ax2.legend()
ax2.grid(True, alpha=0.3)
ax2.invert_xaxis()

# Memory usage
for i, algo in enumerate(df['Algorithm'].unique()):
    data = df[df['Algorithm'] == algo].sort_values('SupportPoint')
    ax3.plot(data['SupportPoint'], data['Memory(MB)'],
             marker='^', linewidth=3, markersize=8,
             color=colors[i % len(colors)], label=algo, alpha=0.8)

ax3.set_xlabel('Support Point (%)', fontweight='bold')
ax3.set_ylabel('Memory Usage (MB)', fontweight='bold')
ax3.set_title('💾 Memory Efficiency', fontsize=14, fontweight='bold')
ax3.legend()
ax3.grid(True, alpha=0.3)
ax3.invert_xaxis()

# Efficiency (patterns per second)
for i, algo in enumerate(df['Algorithm'].unique()):
    data = df[df['Algorithm'] == algo].sort_values('SupportPoint')
    efficiency = data['PatternsFound'] / (data['Runtime(ms)'] / 1000)
    ax4.plot(data['SupportPoint'], efficiency,
             marker='D', linewidth=3, markersize=8,
             color=colors[i % len(colors)], label=algo, alpha=0.8)

ax4.set_xlabel('Support Point (%)', fontweight='bold')
ax4.set_ylabel('Patterns/Second', fontweight='bold')
ax4.set_title('⚡ Algorithm Efficiency', fontsize=14, fontweight='bold')
ax4.legend()
ax4.grid(True, alpha=0.3)
ax4.invert_xaxis()

plt.tight_layout()
plt.savefig('top3_comparison.png', dpi=300, bbox_inches='tight')
plt.show()

print('📊 Chart saved: top3_comparison.png')
print('🎯 Size: {}-{} items'.format(1, 5))
print('🔥 Mode: MIXED (frequent + rare items)')
