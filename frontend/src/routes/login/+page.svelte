<script lang="ts">
    import { goto } from '$app/navigation';
    import { ownerLogin, userLogin } from '$lib/session';

    let username = '';
    let password = '';
    let error = '';
    let isSubmitting = false;

    async function submit() {
        error = '';
        if (!username.trim() || !password) {
            error = 'Enter a username and password.';
            return;
        }

        isSubmitting = true;
        try {
            // Try user login first; if that fails and username is owner, fall back to owner login
            try {
                await userLogin(username.trim(), password);
            } catch (err) {
                if (username.trim().toLowerCase() === 'owner') {
                    await ownerLogin(password);
                } else {
                    throw err;
                }
            }
            await goto('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Login failed';
        } finally {
            isSubmitting = false;
        }
    }
</script>

<div class="flex flex-col items-center mt-20 gap-6">
    <div class="login-card">
        <div class="login-hero">
            <div>
                <p class="eyebrow">Welcome back</p>
                <h1>Sign in to Amazin</h1>
                <p class="subtext">Access your cart, manage your books, and keep browsing.</p>
            </div>
            <img src="/amazin.png" alt="Amazin logo" />
        </div>

        <div class="fields">
            <input
                bind:value={username}
                placeholder="Username"
                class="input"
                autocomplete="username"
            />

            <input
                type="password"
                bind:value={password}
                placeholder="Password"
                class="input"
                autocomplete="current-password"
            />
        </div>

        <button
            class="primary-btn"
            on:click={submit}
            disabled={isSubmitting}
        >
            {isSubmitting ? 'Signing in...' : 'Login'}
        </button>

        {#if error}
            <p class="error">{error}</p>
        {/if}
    </div>
</div>

<style>
  :global(body) {
    background: linear-gradient(135deg, #f5f7fb 0%, #eef2ff 100%);
  }

  .login-card {
    background: #ffffff;
    box-shadow: 0 12px 40px -24px rgba(15, 23, 42, 0.5);
    border-radius: 18px;
    padding: 2.5rem;
    width: min(460px, 92vw);
    display: flex;
    flex-direction: column;
    gap: 1rem;
    border: 1px solid #e5e7eb;
  }

  .login-hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }

  .login-hero h1 {
    font-size: 1.7rem;
    font-weight: 700;
    margin: 0.15rem 0;
    color: #111827;
  }

  .login-hero img {
    width: 92px;
    height: auto;
  }

  .eyebrow {
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-size: 0.75rem;
    font-weight: 700;
    color: #2563eb;
    margin: 0;
  }

  .subtext {
    margin: 0;
    color: #4b5563;
    font-size: 0.95rem;
  }

  .fields {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }

  .input {
    border: 1px solid #d1d5db;
    border-radius: 12px;
    padding: 0.9rem 1rem;
    font-size: 1rem;
    transition: border-color 0.15s, box-shadow 0.15s;
    background: #f9fafb;
  }

  .input:focus {
    outline: none;
    border-color: #2563eb;
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
    background: #fff;
  }

  .primary-btn {
    width: 100%;
    margin-top: 1rem;
    background: linear-gradient(135deg, #2563eb, #1d4ed8);
    color: #fff;
    border: none;
    border-radius: 12px;
    padding: 0.95rem;
    font-weight: 700;
    font-size: 1rem;
    cursor: pointer;
    transition: transform 0.1s ease, box-shadow 0.15s ease, opacity 0.15s;
  }

  .primary-btn:hover:enabled {
    transform: translateY(-1px);
    box-shadow: 0 10px 30px -18px rgba(37, 99, 235, 0.7);
  }

  .primary-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .error {
    color: #dc2626;
    text-align: center;
    margin: 0.5rem 0 0;
    font-weight: 600;
  }

  @media (max-width: 520px) {
    .login-hero {
      flex-direction: column;
      align-items: flex-start;
    }

    .login-hero img {
      align-self: flex-start;
    }
  }
</style>
